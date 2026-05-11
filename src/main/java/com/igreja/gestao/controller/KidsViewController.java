package com.igreja.gestao.controller;

import com.igreja.gestao.model.Crianca;
import com.igreja.gestao.repository.CriancaRepository;
import com.igreja.gestao.repository.MembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/kids")
public class KidsViewController {

    @Autowired
    private CriancaRepository criancaRepository;

    @Autowired
    private MembroRepository membroRepository;

    // =======================================================
    //                    ÁREA ADMINISTRATIVA
    // =======================================================

    @GetMapping("/monitor")
    public String abrirMonitor() {
        return "kids/monitor";
    }

    @GetMapping("/gestao")
    public String listar(Model model) {
        model.addAttribute("criancas", criancaRepository.findAll(Sort.by("nomeCompleto")));
        return "kids/lista";
    }

    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("crianca", new Crianca());
        model.addAttribute("responsaveis", membroRepository.findAll(Sort.by("nome")));
        return "kids/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Crianca crianca = criancaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Criança inválida: " + id));

        model.addAttribute("crianca", crianca);
        model.addAttribute("responsaveis", membroRepository.findAll(Sort.by("nome")));
        return "kids/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(Crianca crianca) {
        criancaRepository.save(crianca);
        return "redirect:/kids/gestao";
    }

    // --- Tratamento de erro na exclusão ---
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            criancaRepository.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Cadastro da criança excluído com sucesso.");
        } catch (DataIntegrityViolationException e) {
            // Captura o bloqueio do PostgreSQL e envia para a tela nosso alerta premium
            attributes.addFlashAttribute("mensagemErro", "Não é possível excluir esta criança porque ela já possui histórico de presenças e check-ins vinculados.");
        }
        return "redirect:/kids/gestao";
    }

    // =======================================================
    // ÁREA DO CHECK-IN (CELULAR DO PAI)
    // =======================================================

    @GetMapping("/checkin")
    public String telaCheckin() {
        return "kids/checkin-busca";
    }

    @PostMapping("/checkin/buscar")
    public String buscarFilhos(@RequestParam(value = "telefone", required = false) String telefone, Model model) {

        // 1. Validação: Se o campo veio vazio
        if (telefone == null || telefone.trim().isEmpty()) {
            model.addAttribute("erro", "Por favor, digite o número do celular.");
            return "kids/checkin-busca";
        }

        // 2. Limpa formatação
        String telefoneLimpo = telefone.replaceAll("\\D", "");

        // 3. Busca no banco
        List<Crianca> filhos = criancaRepository.buscarPorTelefoneLimpo(telefoneLimpo);

        // 4. Se não achar nada
        if (filhos.isEmpty()) {
            model.addAttribute("erro", "Não encontramos cadastro para este número (" + telefone + ")");
            model.addAttribute("telefone", telefone); // Devolve o número pro pai corrigir
            return "kids/checkin-busca";
        }

        // 5. Sucesso: Vai para a tela de seleção
        model.addAttribute("filhos", filhos);
        model.addAttribute("telefone", telefone);

        return "kids/checkin-selecao";
    }

    @GetMapping("/checkin/sucesso")
    public String checkinSucesso() {
        return "kids/checkin-sucesso";
    }

    @GetMapping("/saida")
    public String telaSaida() {
        return "kids/saida";
    }
}