package com.igreja.gestao.controller;

import com.igreja.gestao.model.Ministerio;
import com.igreja.gestao.repository.MinisterioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ministerios")
public class MinisterioController {

    @Autowired
    private MinisterioRepository ministerioRepository;

    // FORMULÁRIO PARA CRIAR/EDITAR UMA ÁREA (EX: SOM, INFANTIL)
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("ministerio", new Ministerio());
        return "equipes/form_ministerio";
    }

    // EDITAR UMA ÁREA EXISTENTE
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Ministerio ministerio = ministerioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido"));
        model.addAttribute("ministerio", ministerio);
        return "equipes/form_ministerio";
    }

    // SALVAR
    @PostMapping("/salvar")
    public String salvar(Ministerio ministerio) {
        ministerioRepository.save(ministerio);
        return "redirect:/equipes";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        try {
            ministerioRepository.deleteById(id);
        } catch (Exception e) {
            // Se tiver vínculo, vai dar erro
            System.out.println("Erro ao excluir: pode ter membros vinculados.");
        }
        return "redirect:/equipes";
    }
}