package com.igreja.gestao.controller;

import com.igreja.gestao.model.Usuario;
import com.igreja.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// --- IMPORTS NOVOS ADICIONADOS PARA O "MEU PERFIL" ---
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
// -----------------------------------------------------

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // DEFINIÇÃO DAS PERMISSÕES DO SISTEMA (AQUI ESTAVA O SEGREDO!)
    private final List<String> permissoesDisponiveis = List.of(
            "ADMIN",          // Manda em tudo
            "FINANCEIRO",     // Acessa Tesouraria
            "SECRETARIA",     // Acessa Membros
            "SOCIAL",         // Acessa Famílias
            "ESTOQUE",        // Acessa Produtos
            "AGENDA",         // Acessa Eventos
            "MINISTERIO",     // Acessa Equipes
            "GESTAO_EQUIPES", // Acessa Gestao Equipes
            "MODELOS_CULTOS", // Acessa Modelo de Cultos
            "RECEPCAO"        // <-- AQUI! Acessa o Módulo de Boas Vindas
    );

    // LISTA USUÁRIOS
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios/lista";
    }

    // NOVO USUÁRIO
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("listaPerfis", permissoesDisponiveis);
        return "usuarios/formulario";
    }

    // EDITAR USUÁRIO
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("listaPerfis", permissoesDisponiveis);

        return "usuarios/formulario";
    }

    // SALVAR COM LÓGICA DE SENHA E PERMISSÕES
    @PostMapping("/salvar")
    public String salvar(Usuario usuario) {

        // 1. Lógica de Senha (para não sobrescrever com vazio na edição)
        if (usuario.getId() != null) {
            if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
                // Se a senha veio vazia, busca a antiga no banco e mantém
                Usuario usuarioAntigo = usuarioRepository.findById(usuario.getId()).orElse(null);
                if (usuarioAntigo != null) {
                    usuario.setSenha(usuarioAntigo.getSenha());
                }
            } else {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }
        } else {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        usuarioRepository.save(usuario);
        return "redirect:/usuarios";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/usuarios";
    }


    @GetMapping("/meu-perfil")
    public String abrirMeuPerfil(Principal principal, Model model) {
        String loginLogado = principal.getName();

        Usuario usuario = usuarioRepository.findByLogin(loginLogado).orElse(null);

        model.addAttribute("usuario", usuario);
        return "usuarios/meu-perfil";
    }

    // 2. SALVA A NOVA SENHA DA PRÓPRIA PESSOA (URL final: /usuarios/meu-perfil/alterar-senha)
    @PostMapping("/meu-perfil/alterar-senha")
    public String alterarSenha(Principal principal,
                               @RequestParam("novaSenha") String novaSenha,
                               @RequestParam("confirmarSenha") String confirmarSenha,
                               RedirectAttributes attributes) {

        if (!novaSenha.equals(confirmarSenha)) {
            attributes.addFlashAttribute("mensagemErro", "As senhas não conferem. Tente novamente.");
            return "redirect:/usuarios/meu-perfil";
        }

        Usuario usuario = usuarioRepository.findByLogin(principal.getName()).orElse(null);

        if (usuario != null) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuarioRepository.save(usuario);
            attributes.addFlashAttribute("mensagemSucesso", "Sua senha foi atualizada com segurança!");
        } else {
            attributes.addFlashAttribute("mensagemErro", "Erro ao localizar usuário logado.");
        }

        return "redirect:/usuarios/meu-perfil";
    }
}