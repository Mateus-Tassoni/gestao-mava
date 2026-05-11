package com.igreja.gestao.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Rota direta para o Painel TV
    @GetMapping("/painel")
    public String abrirPainel() {
        return "presenca/painel";
    }
}
