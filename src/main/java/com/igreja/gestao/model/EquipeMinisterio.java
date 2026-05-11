package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;


@Data
@Entity
@Table(name = "equipes_ministerio")
public class EquipeMinisterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @ToString.Exclude
    private Membro membro;

    @ManyToOne
    @ToString.Exclude
    private Ministerio ministerio;

    @Enumerated(EnumType.STRING)
    private FuncaoMinisterio funcao;

    public enum FuncaoMinisterio {
        LIDER,
        MEMBRO,
        VOLUNTARIO
    }
}