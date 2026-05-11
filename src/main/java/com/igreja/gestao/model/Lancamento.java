package com.igreja.gestao.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    @NumberFormat(pattern = "#,##0.00")
    private BigDecimal valor;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    @ManyToOne
    @JoinColumn(name = "membro_id", nullable = true)
    private Membro membro;

    // --- NOVO: CAMPO PARA SALVAR O NOME DIGITADO SE NÃO FOR MEMBRO ---
    private String nomeVisitante;

    public enum TipoLancamento {
        DIZIMO(1), OFERTA(1), DOACAO(1),
        AGUA_LUZ(-1), MANUTENCAO(-1), ALUGUEL(-1), SOCIAL(-1), OUTROS(-1);

        private final int fator;
        TipoLancamento(int fator) { this.fator = fator; }
        public int getFator() { return fator; }
    }

    // --- MÁGICA PRO FRONT-END ---
    // Vai facilitar a sua vida na hora de montar a tabela de extrato!
    public String getNomeExibicao() {
        if (this.membro != null && this.membro.getNome() != null && !this.membro.getNome().isEmpty()) {
            return this.membro.getNome();
        }
        return this.nomeVisitante;
    }

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }

    public Membro getMembro() { return membro; }
    public void setMembro(Membro membro) { this.membro = membro; }

    public String getNomeVisitante() { return nomeVisitante; }
    public void setNomeVisitante(String nomeVisitante) { this.nomeVisitante = nomeVisitante; }
}