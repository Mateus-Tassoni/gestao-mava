package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "tipos_evento")
public class TipoEvento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToMany
    @JoinTable(
            name = "padrao_estrutura_evento",
            joinColumns = @JoinColumn(name = "tipo_evento_id"),
            inverseJoinColumns = @JoinColumn(name = "ministerio_id")
    )
    private List<Ministerio> estruturaPadrao;
}