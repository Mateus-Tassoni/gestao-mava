package com.igreja.gestao.controller;

import com.igreja.gestao.model.Evento;
import com.igreja.gestao.repository.EventoRepository;
import com.igreja.gestao.repository.TipoEventoRepository;
import com.igreja.gestao.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Autowired
    private EventoService eventoService;

    // TELA DE LISTA (Mostra todos para gestão)
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("eventos", eventRepository.findAll(Sort.by(Sort.Direction.ASC, "dataHoraInicio")));
        return "eventos/lista";
    }

    // TELA DE PENDENTES (Exclusiva para o Pastor aprovar)
    @GetMapping("/pendentes")
    public String listarPendentes(Model model) {
        model.addAttribute("eventos", eventRepository.findByStatus(Evento.StatusEvento.PENDENTE));
        return "eventos/pendentes";
    }

    // TELA DE FORMULÁRIO
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("tiposEvento", tipoEventoRepository.findAll());
        return "eventos/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(Evento evento,
                         @RequestParam(value = "repeticoes", defaultValue = "0") int repeticoes,
                         RedirectAttributes attributes) {

        // 1. Definição Inicial de Status
        if (evento.getId() == null) {
            evento.setStatus(Evento.StatusEvento.PENDENTE);
        }

        try {
            // 2. Salva o Evento Principal (Dia Atual)
            salvarComValidacao(evento);

            // 3. Lógica de Repetição (TRAVA AS PRÓXIMAS DATAS)
            if (repeticoes > 0 && evento.getId() != null) {
                for (int i = 1; i <= repeticoes; i++) {
                    Evento clone = new Evento();
                    clone.setNome(evento.getNome());
                    clone.setLocal(evento.getLocal());
                    clone.setDescricao(evento.getDescricao());
                    clone.setTipoEvento(evento.getTipoEvento());
                    clone.setStatus(evento.getStatus()); // Mantém o status do pai

                    clone.setDataHoraInicio(evento.getDataHoraInicio().plusWeeks(i));
                    clone.setDataHoraFim(evento.getDataHoraFim().plusWeeks(i));

                    // Tenta salvar a cópia (se tiver conflito nessa data futura, o erro para tudo)
                    salvarComValidacao(clone);
                }
            }

            if (repeticoes > 0) {
                attributes.addFlashAttribute("mensagemSucesso", "Evento criado e repetido por " + repeticoes + " semanas com sucesso!");
            } else {
                attributes.addFlashAttribute("mensagemSucesso", "Solicitação enviada com sucesso!");
            }

        } catch (IllegalStateException e) {
            // Captura o erro de conflito vindo do método auxiliar
            attributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/eventos/novo";
        }

        return "redirect:/eventos";
    }

    // --- MÉTODO AUXILIAR DE SEGURANÇA ---
    private void salvarComValidacao(Evento evento) {
        // Verifica se JÁ EXISTE um evento APROVADO no horário
        boolean conflito = eventRepository.verificarConflito(evento.getDataHoraInicio(), evento.getDataHoraFim());

        if (conflito) {
            // Formata a data para mostrar qual dia deu problema
            String dataConflito = evento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm"));
            throw new IllegalStateException("BLOQUEIO: O horário de " + dataConflito + " já está reservado/travado.");
        }

        eventoService.salvarComLogica(evento);
    }

    // APROVAR EVENTO (Gatilho para o WhatsApp)
    @PostMapping("/aprovar/{id}")
    public String aprovar(@PathVariable Long id, RedirectAttributes attributes) {
        Evento evento = eventRepository.findById(id).orElseThrow();
        evento.setStatus(Evento.StatusEvento.APROVADO);
        eventRepository.save(evento);

        eventoService.notificarAprovacao(evento);

        attributes.addFlashAttribute("mensagemSucesso", "Evento aprovado e datas travadas!");
        return "redirect:/eventos/pendentes";
    }

    // REJEITAR EVENTO
    @PostMapping("/rejeitar/{id}")
    public String rejeitar(@PathVariable Long id, RedirectAttributes attributes) {
        Evento evento = eventRepository.findById(id).orElseThrow();
        evento.setStatus(Evento.StatusEvento.REJEITADO);
        eventRepository.save(evento);

        attributes.addFlashAttribute("mensagemInfo", "Evento rejeitado.");
        return "redirect:/eventos/pendentes";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/eventos";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Evento evento = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento inválido: " + id));
        model.addAttribute("evento", evento);
        return "eventos/detalhe";
    }

    @GetMapping("/mapa")
    public String mapaCalendario() {
        return "eventos/calendario";
    }
}