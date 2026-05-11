package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Familia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeRepresentante;

    @Column(unique = true)
    private String documento;
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    @Column(columnDefinition = "TEXT")
    private String perfilSocial;

    @OneToMany(mappedBy = "familiaAssistida")
    private List<MovimentacaoEstoque> historicoAjuda;
}