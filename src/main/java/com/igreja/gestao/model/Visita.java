package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataVisita;

    // --- QUEM ESTÁ RECEBENDO A VISITA ---
    @ManyToOne
    @JoinColumn(name = "membro_id", nullable = true)
    private Membro membro;

    // Usado caso a visita seja para alguém que ainda não é membro (visitante do culto, familiar, etc)
    private String nomeNaoMembro;

    private String telefoneContato; // Essencial para contato via WhatsApp/QR Code

    // --- QUEM ESTÁ FAZENDO A VISITA --- (Agora conectado ao cadastro real)
    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Membro responsavel;

    @Enumerated(EnumType.STRING)
    private TipoVisita tipo;

    @Enumerated(EnumType.STRING)
    private StatusVisita status;

    // MANTIDO APENAS PARA COMPATIBILIDADE COM O BANCO DE DADOS ANTIGO
    // Isso evita o erro 'violates not-null constraint' no PostgreSQL
    private boolean realizada = false;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Enums para padronizar o banco e facilitar gráficos/relatórios no futuro
    public enum TipoVisita {
        ACONSELHAMENTO,
        ENFERMIDADE_HOSPITAL,
        NOVO_VISITANTE, // Aquele visitante que foi no culto e o pastor vai na casa dele depois
        ROTINA_PASTORAL,
        OUTROS
    }

    public enum StatusVisita {
        PENDENTE, // Caiu pelo QR Code, aguardando triagem da secretaria/pastor
        AGENDADA,
        REALIZADA,
        CANCELADA
    }
}