package com.igreja.gestao.model;

import com.igreja.gestao.model.enums.EstadoCivil;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Entity
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataBatismo;

    // --- MANTIDO: DATA DE ACLAMAÇÃO ---
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataAclamacao;

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    private Cargo cargo = Cargo.MEMBRO;

    @Enumerated(EnumType.STRING)
    private StatusMembro status = StatusMembro.ATIVO;

    private String fotoUrl;

    public enum Cargo {
        MEMBRO, OBREIRO, DIACONO, PRESBITERO, EVANGELISTA, PASTOR, MISSIONARIO, MUSICO
    }

    public enum StatusMembro {
        ATIVO, INATIVO, EM_DISCIPLINA, MUDANCA, FALECIDO
    }
}