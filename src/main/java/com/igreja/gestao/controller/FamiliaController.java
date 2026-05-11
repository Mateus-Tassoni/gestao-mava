package com.igreja.gestao.controller;

import com.igreja.gestao.model.Familia;
import com.igreja.gestao.model.MovimentacaoEstoque;
import com.igreja.gestao.repository.FamiliaRepository;
import com.igreja.gestao.repository.MovimentacaoEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/familias")
public class FamiliaController {

    @Autowired
    private FamiliaRepository familiaRepository;

    // histórico de doações
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    // Listar todas as famílias
    @GetMapping
    public String listarFamilias(Model model) {
        model.addAttribute("familias", familiaRepository.findAll());
        return "familias/lista";
    }

    // Abrir formulário de cadastro (Nova)
    @GetMapping("/nova")
    public String novaFamilia(Model model) {
        model.addAttribute("familia", new Familia());
        return "familias/cadastro";
    }

    @GetMapping("/editar/{id}")
    public String editarFamilia(@PathVariable Long id, Model model) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Família inválida: " + id));

        // Manda a família encontrada para a mesma tela de cadastro
        model.addAttribute("familia", familia);
        return "familias/cadastro";
    }

    @PostMapping("/salvar")
    public String salvarFamilia(Familia familia) {

        if (familia.getDocumento() != null && familia.getDocumento().trim().isEmpty()) {
            familia.setDocumento(null);
        }

        familiaRepository.save(familia);
        return "redirect:/familias";
    }

    // Ver Histórico
    @GetMapping("/{id}/historico")
    public String historicoFamilia(@PathVariable Long id, Model model) {
        // Busca a família
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Família inválida: " + id));

        // Busca todas as doações que essa família já recebeu
        List<MovimentacaoEstoque> historico = movimentacaoRepository.findByFamiliaAssistidaIdOrderByDataHoraDesc(id);

        // Manda tudo para o HTML
        model.addAttribute("familia", familia);
        model.addAttribute("historico", historico);

        return "familias/historico";
    }

    // ==========================================
    // EXCLUIR FAMÍLIA COM PROTEÇÃO
    // ==========================================
    @GetMapping("/excluir/{id}")
    public String excluirFamilia(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            familiaRepository.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Família excluída com sucesso.");
        } catch (DataIntegrityViolationException e) {
            // Se a família já recebeu alguma doação do estoque, o banco bloqueia a exclusão
            attributes.addFlashAttribute("mensagemErro", "Não é possível excluir esta família pois ela já possui histórico de cestas ou doações recebidas no estoque.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao tentar excluir a família.");
        }
        return "redirect:/familias";
    }
}