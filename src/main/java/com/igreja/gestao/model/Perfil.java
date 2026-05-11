package com.igreja.gestao.model;

public enum Perfil {
    ADMIN("Administrador"),
    SECRETARIA("Secretaria"),
    TESOURARIA("Tesouraria"),
    SOCIAL("Ação Social"),
    RECEPCAO("Recepção");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}