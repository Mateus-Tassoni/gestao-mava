package com.igreja.gestao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .authorizeHttpRequests((requests) -> requests
                        // === 1. ARQUIVOS ESTÁTICOS ===
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // === 2. FERRAMENTAS TÉCNICAS (H2, Python, Placar) ===
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/presenca/**").permitAll()
                        .requestMatchers("/painel", "/api/presenca/placar").permitAll()

                        // === 3. MÓDULO KIDS (Acesso Público) ===
                        // Check-in do Pai (Celular) e APIs de Impressão
                        .requestMatchers("/kids/checkin/**", "/api/kids/**", "/api/impressao/**").permitAll()

                        // IMPORTANTE: Monitor da Recepção (Precisa acessar sem logar às vezes)
                        .requestMatchers("/kids/monitor").permitAll()

                        // === 4. MÓDULO PASTORAL / QR CODE (Acesso Público) ===
                        // Solicitação de visita pelo celular sem precisar de senha
                        .requestMatchers("/solicitar-visita/**").permitAll()

                        // === 5. A PORTA DE ENTRADA (O PORTEIRO) ===
                        // Liberamos a raiz "/" para o HomeController decidir para onde redirecionar
                        .requestMatchers("/").permitAll()

                        // === 6. TODO O RESTO (Privado / Administrativo) ===
                        // Aqui inclui /dashboard, /membros, /financeiro, etc.
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**", "/favicon.ico"
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}