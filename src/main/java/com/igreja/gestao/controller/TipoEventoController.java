package com.igreja.gestao.controller;

import com.igreja.gestao.model.TipoEvento;
import com.igreja.gestao.repository.MinisterioRepository;
import com.igreja.gestao.repository.TipoEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/config/tipos")
public class TipoEventoController {

    @Autowired
    private TipoEventoRepository tipoRepository;

    @Autowired
    private MinisterioRepository ministerioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoRepository.findAll());
        return "config/tipos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tipoEvento", new TipoEvento());
        model.addAttribute("todosMinisterios", ministerioRepository.findAll());
        return "config/tipos/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(TipoEvento tipoEvento) {
        tipoRepository.save(tipoEvento);
        return "redirect:/config/tipos";
    }

    // --- ROTAS DE EDIÇÃO E EXCLUSÃO ---

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        TipoEvento tipoEvento = tipoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));

        // Reutilizamos as mesmas variáveis do método 'novo'
        // para a mesma tela de formulário abrir preenchida
        model.addAttribute("tipoEvento", tipoEvento);
        model.addAttribute("todosMinisterios", ministerioRepository.findAll());
        return "config/tipos/formulario";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            tipoRepository.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Modelo de culto excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // Se o banco bloquear por causa da chave estrangeira (já existe evento com esse modelo), ele cai aqui:
            attributes.addFlashAttribute("mensagemErro", "Não é possível excluir este modelo porque já existem eventos agendados usando ele. Tente editá-lo em vez de excluir.");
        }
        return "redirect:/config/tipos";
    }
}