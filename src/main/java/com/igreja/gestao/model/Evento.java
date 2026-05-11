package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String local;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraFim;

    @ManyToOne
    @JoinColumn(name = "tipo_evento_id")
    private TipoEvento tipoEvento;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstruturaEvento> estrutura = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private StatusEvento status = StatusEvento.PENDENTE;

    public enum StatusEvento {
        PENDENTE, APROVADO, REJEITADO
    }

    public void adicionarNaEstrutura(Ministerio ministerio) {
        EstruturaEvento item = new EstruturaEvento(this, ministerio);
        this.estrutura.add(item);
    }
}