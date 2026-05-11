package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private CategoriaProduto categoria;

    private String unidade;

    private int quantidadeAtual;

    private int estoqueMinimo;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    public enum CategoriaProduto {
        ALIMENTOS,
        LIMPEZA,
        HIGIENE,
        VESTUARIO,
        OUTROS
    }
}