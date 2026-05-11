package com.igreja.gestao.controller;

import com.igreja.gestao.model.EquipeMinisterio;
import com.igreja.gestao.model.Ministerio;
import com.igreja.gestao.repository.EquipeMinisterioRepository;
import com.igreja.gestao.repository.MembroRepository;
import com.igreja.gestao.repository.MinisterioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/equipes")
public class EquipeController {

    @Autowired
    private EquipeMinisterioRepository equipeRepository;

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private MinisterioRepository ministerioRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("ministerios", ministerioRepository.findAll());
        return "equipes/dashboard"; // Mudamos o nome do arquivo HTML
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long ministerioId, Model model) {
        EquipeMinisterio equipe = new EquipeMinisterio();

        if (ministerioId != null) {
            Ministerio m = ministerioRepository.findById(ministerioId).orElse(null);
            equipe.setMinisterio(m);
        }

        model.addAttribute("equipeMinisterio", equipe);
        model.addAttribute("membros", membroRepository.findAll());
        model.addAttribute("ministerios", ministerioRepository.findAll());
        return "equipes/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(EquipeMinisterio equipeMinisterio) {
        equipeRepository.save(equipeMinisterio);
        return "redirect:/equipes";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        equipeRepository.deleteById(id);
        return "redirect:/equipes";
    }
}