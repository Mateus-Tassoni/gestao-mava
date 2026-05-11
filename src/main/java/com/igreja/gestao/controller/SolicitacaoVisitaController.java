package com.igreja.gestao.controller;

import com.igreja.gestao.model.Visita;
import com.igreja.gestao.repository.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SolicitacaoVisitaController {

    @Autowired
    private VisitaRepository visitaRepository;

    // Rota pública para o QR Code
    @GetMapping("/solicitar-visita")
    public String telaSolicitacao(Model model) {
        model.addAttribute("visita", new Visita());
        return "visitas/solicitacao-publica"; // Criaremos esse HTML focado em celular
    }

    // Rota que salva o pedido feito pelo celular
    @PostMapping("/solicitar-visita/salvar")
    public String salvarSolicitacao(Visita visita, RedirectAttributes attributes) {
        // Força o status como PENDENTE para aparecer como alerta para a secretaria
        visita.setStatus(Visita.StatusVisita.PENDENTE);

        visitaRepository.save(visita);

        attributes.addFlashAttribute("mensagemSucesso", "Sua solicitação foi enviada com sucesso! Nossa equipe entrará em contato.");
        return "redirect:/solicitar-visita";
    }
}