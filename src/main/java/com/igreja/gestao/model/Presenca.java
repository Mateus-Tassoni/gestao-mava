package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "presencas")
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    private String tipo; // "MEMBRO" ou "VISITANTE"

    @ManyToOne
    @JoinColumn(name = "membro_id")
    private Membro membro;

    public Presenca(Membro membro, String tipo) {
        this.membro = membro;
        this.tipo = tipo;
        this.dataHora = LocalDateTime.now();
    }
}