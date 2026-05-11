package com.igreja.gestao;

import com.igreja.gestao.model.Usuario;
import com.igreja.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CarregadorDados implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Se não tiver nenhum usuário, cria os padrões
        if (usuarioRepository.count() == 0) {

            // 1. ADMIN (Acesso total)
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin("admin");
            admin.setSenha(passwordEncoder.encode("123456"));
            admin.addPerfil("ADMIN"); // Usa o novo método addPerfil
            usuarioRepository.save(admin);

            // 2. TESOUREIRO (Acesso Financeiro)
            Usuario tesoureiro = new Usuario();
            tesoureiro.setNome("Tesoureiro Principal");
            tesoureiro.setLogin("financeiro");
            tesoureiro.setSenha(passwordEncoder.encode("123456"));
            // ATENÇÃO: Mudamos de "TESOURARIA" para "FINANCEIRO" para bater com o SecurityConfig
            tesoureiro.addPerfil("FINANCEIRO");
            usuarioRepository.save(tesoureiro);

            // 3. AÇÃO SOCIAL (Cozinha/Cestas)
            Usuario cozinha = new Usuario();
            cozinha.setNome("Líder da Cantina");
            cozinha.setLogin("cozinha");
            cozinha.setSenha(passwordEncoder.encode("123456"));
            cozinha.addPerfil("SOCIAL");
            usuarioRepository.save(cozinha);

            System.out.println("--- USUÁRIOS PADRÃO CRIADOS ---");
            System.out.println(">>> Admin: admin / 123456");
            System.out.println(">>> Financeiro: financeiro / 123456");
            System.out.println(">>> Social: cozinha / 123456");
        }
    }
}