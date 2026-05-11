package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private int quantidade;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHora;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataValidade;

    @ManyToOne
    @JoinColumn(name = "familia_id")
    private Familia familiaAssistida;

    private String responsavelRetirada;

    private String observacao;
}