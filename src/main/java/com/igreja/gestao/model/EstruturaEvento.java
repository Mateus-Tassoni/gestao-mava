package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class EstruturaEvento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "ministerio_id")
    private Ministerio ministerio;

    private boolean notificado = false;

    public EstruturaEvento(Evento evento, Ministerio ministerio) {
        this.evento = evento;
        this.ministerio = ministerio;
    }
}