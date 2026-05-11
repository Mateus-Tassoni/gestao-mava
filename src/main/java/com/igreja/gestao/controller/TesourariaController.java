package com.igreja.gestao.controller;

import com.igreja.gestao.model.Lancamento;
import com.igreja.gestao.model.Lancamento.TipoLancamento;
import com.igreja.gestao.model.Membro;
import com.igreja.gestao.repository.LancamentoRepository;
import com.igreja.gestao.repository.MembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tesouraria")
public class TesourariaController {

    @Autowired
    private LancamentoRepository repository;

    @Autowired
    private MembroRepository membroRepository;

    // LISTAR COM FILTRO DE MÊS/ANO E CÁLCULO DE SALDO
    @GetMapping
    public String listar(
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            Model model) {

        LocalDate hoje = LocalDate.now();
        int mesFiltro = (mes != null) ? mes : hoje.getMonthValue();
        int anoFiltro = (ano != null) ? ano : hoje.getYear();

        LocalDate dataInicio = LocalDate.of(anoFiltro, mesFiltro, 1);
        LocalDate dataFim = dataInicio.withDayOfMonth(dataInicio.lengthOfMonth());

        List<Lancamento> lancamentos = repository.findByDataBetweenOrderByDataDesc(dataInicio, dataFim);
        model.addAttribute("lancamentos", lancamentos);

        BigDecimal entradas = lancamentos.stream()
                .filter(l -> l.getTipo().getFator() > 0)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidas = lancamentos.stream()
                .filter(l -> l.getTipo().getFator() < 0)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = entradas.subtract(saidas);

        model.addAttribute("totalEntradas", entradas);
        model.addAttribute("totalSaidas", saidas);
        model.addAttribute("saldo", saldo);
        model.addAttribute("mesAtual", mesFiltro);
        model.addAttribute("anoAtual", anoFiltro);

        return "tesouraria/lista";
    }

    // NOVO LANÇAMENTO
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("lancamento", new Lancamento());
        model.addAttribute("membros", membroRepository.buscarTodosOrdemAlfabeticaForcada());
        model.addAttribute("tipos", TipoLancamento.values());

        return "tesouraria/formulario";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Lancamento lancamento = repository.findById(id).orElse(null);
        model.addAttribute("lancamento", lancamento);
        model.addAttribute("membros", membroRepository.buscarTodosOrdemAlfabeticaForcada());
        model.addAttribute("tipos", TipoLancamento.values());
        return "tesouraria/formulario";
    }

    // SALVAR (COM INTELIGÊNCIA DE BUSCA, VÍNCULO E RECORRÊNCIA)
    @PostMapping("/salvar")
    public String salvar(Lancamento lancamento,
                         @RequestParam(value = "nomeInput", required = false) String nomeInput,
                         @RequestParam(value = "qtdMeses", defaultValue = "1") int qtdMeses,
                         RedirectAttributes attributes) {

        Membro membroVinculado = null;
        String visitanteNome = null;

        // Se o cara digitou algo no campo de busca da tela
        if (nomeInput != null && !nomeInput.trim().isEmpty()) {
            String nomeBusca = nomeInput.trim();

            // Varre o banco procurando alguém com o nome exato
            Optional<Membro> membroEncontrado = membroRepository.findAll().stream()
                    .filter(m -> m.getNome().equalsIgnoreCase(nomeBusca))
                    .findFirst();

            if (membroEncontrado.isPresent()) {
                membroVinculado = membroEncontrado.get();
            } else {
                visitanteNome = nomeBusca;
            }
        }

        // --- VERIFICA SE É EDIÇÃO OU NOVO CADASTRO ---
        if (lancamento.getId() != null) {
            // É EDIÇÃO: Salva apenas o atual, ignora a recorrência
            lancamento.setMembro(membroVinculado);
            lancamento.setNomeVisitante(visitanteNome);
            repository.save(lancamento);
            attributes.addFlashAttribute("mensagemSucesso", "Lançamento atualizado com sucesso!");

        } else {
            // É NOVO: Executa o laço de repetição para os meses informados
            for (int i = 0; i < qtdMeses; i++) {
                Lancamento parcela = new Lancamento();

                // Se for mais de 1 mês, ele adiciona "(1/12)" na descrição para você saber que é parcelado
                if (qtdMeses > 1) {
                    parcela.setDescricao(lancamento.getDescricao() + " (" + (i + 1) + "/" + qtdMeses + ")");
                } else {
                    parcela.setDescricao(lancamento.getDescricao());
                }

                // A MÁGICA DA DATA: Soma os meses automaticamente
                parcela.setData(lancamento.getData().plusMonths(i));

                parcela.setValor(lancamento.getValor());
                parcela.setTipo(lancamento.getTipo());
                parcela.setObservacao(lancamento.getObservacao());
                parcela.setMembro(membroVinculado);
                parcela.setNomeVisitante(visitanteNome);

                repository.save(parcela);
            }
            attributes.addFlashAttribute("mensagemSucesso", qtdMeses > 1 ? qtdMeses + " lançamentos gerados com sucesso!" : "Lançamento salvo com sucesso!");
        }

        return "redirect:/tesouraria";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        repository.deleteById(id);
        attributes.addFlashAttribute("mensagemSucesso", "Lançamento excluído com sucesso.");
        return "redirect:/tesouraria";
    }

    @GetMapping("/api/calendario")
    @ResponseBody
    public List<EventoCalendarioDTO> listarParaCalendario() {
        return repository.findAll().stream()
                .filter(l -> l.getTipo().getFator() < 0)
                .map(l -> {
                    String cor = "#dc3545";
                    String texto = (l.getObservacao() != null && !l.getObservacao().trim().isEmpty())
                            ? l.getObservacao()
                            : l.getTipo().name();

                    String titulo = texto + " (R$ " + l.getValor() + ")";
                    return new EventoCalendarioDTO(titulo, l.getData().toString(), cor);
                }).collect(Collectors.toList());
    }

    public static class EventoCalendarioDTO {
        private String title;
        private String start;
        private String color;

        public EventoCalendarioDTO(String title, String start, String color) {
            this.title = title;
            this.start = start;
            this.color = color;
        }

        public String getTitle() { return title; }
        public String getStart() { return start; }
        public String getColor() { return color; }
    }
}