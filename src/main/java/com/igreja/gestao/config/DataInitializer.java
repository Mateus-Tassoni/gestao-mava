package com.igreja.gestao.config;

import com.igreja.gestao.model.Usuario;
import com.igreja.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();

            // Preenchendo os dados novos
            admin.setNome("Administrador do Sistema");
            admin.setLogin("admin");
            admin.setSenha(passwordEncoder.encode("123456"));
            admin.addPerfil("ADMIN");


            usuarioRepository.save(admin);
            System.out.println("------------------------------------------------------");
            System.out.println(">>> USUÁRIO ADMIN CRIADO COM SUCESSO");
            System.out.println(">>> Login: admin");
            System.out.println(">>> Senha: 123456");
            System.out.println("------------------------------------------------------");
        }
    }
}