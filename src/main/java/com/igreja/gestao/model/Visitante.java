package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Entity
public class Visitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private String telefone;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataCadastro = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private Congregacao congregacao;

    // Enums exclusivos para o Visitante (Estado Civil já pode usar o existente se quiser)
    public enum EstadoCivil {
        SOLTEIRO, CASADO, DIVORCIADO, VIUVO
    }

    public enum Sexo {
        MASCULINO, FEMININO
    }

    public enum Congregacao {
        SEDE
    }
}