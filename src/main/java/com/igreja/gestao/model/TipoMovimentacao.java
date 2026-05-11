package com.igreja.gestao.model;

public enum TipoMovimentacao {
    ENTRADA("Entrada / Compra"),
    DOACAO("Doação Recebida"),
    SAIDA("Distribuição / Doação"),
    CONSUMO("Consumo Interno (Cozinha)"),
    PERDA("Perda / Validade");

    private String descricao;

    TipoMovimentacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}