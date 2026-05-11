package com.igreja.gestao.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Checkin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Crianca crianca;

    @ManyToOne
    private Membro responsavel;

    private LocalDateTime dataHora;
    private String codigoEtiqueta;
    private boolean impresso = false;

    private LocalDateTime dataSaida;

    public Checkin() {
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Crianca getCrianca() { return crianca; }
    public void setCrianca(Crianca crianca) { this.crianca = crianca; }

    public Membro getResponsavel() { return responsavel; }
    public void setResponsavel(Membro responsavel) { this.responsavel = responsavel; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getCodigoEtiqueta() { return codigoEtiqueta; }
    public void setCodigoEtiqueta(String codigoEtiqueta) { this.codigoEtiqueta = codigoEtiqueta; }

    public boolean isImpresso() { return impresso; }
    public void setImpresso(boolean impresso) { this.impresso = impresso; }

    // === MÉTODOS QUE FALTAVAM ===
    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }
}