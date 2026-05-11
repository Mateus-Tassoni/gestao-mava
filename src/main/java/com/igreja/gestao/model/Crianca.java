package com.igreja.gestao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.Period;

@Entity
public class Crianca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCompleto;
    private int idade;
    private LocalDate dataNascimento;
    private String alergias;

    @ManyToOne
    private Membro responsavel;

    private boolean permissaoImagem;      // Ícone: #
    private boolean necessidadesEspeciais; // Ícone: Cadeira/Coração
    private boolean visitante;             // Ícone: Estrela ⭐

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public Membro getResponsavel() { return responsavel; }
    public void setResponsavel(Membro responsavel) { this.responsavel = responsavel; }

    public boolean isPermissaoImagem() { return permissaoImagem; }
    public void setPermissaoImagem(boolean permissaoImagem) { this.permissaoImagem = permissaoImagem; }

    public boolean isNecessidadesEspeciais() { return necessidadesEspeciais; }
    public void setNecessidadesEspeciais(boolean necessidadesEspeciais) { this.necessidadesEspeciais = necessidadesEspeciais; }

    public boolean isVisitante() { return visitante; }
    public void setVisitante(boolean visitante) { this.visitante = visitante; }


    public boolean isAniversarianteSemana() {
        if (dataNascimento == null) return false;
        LocalDate hoje = LocalDate.now();
        LocalDate aniverEsteAno = dataNascimento.withYear(hoje.getYear());

        // Verifica se a data de hoje está próxima do aniversário
        return !hoje.isBefore(aniverEsteAno.minusDays(3)) && !hoje.isAfter(aniverEsteAno.plusDays(3));
    }

    // Ajuda a separar Nome e Sobrenome para a etiqueta
    public String getPrimeiroNome() {
        return nomeCompleto != null ? nomeCompleto.split(" ")[0] : "";
    }

    public String getSobrenome() {
        if (nomeCompleto == null || !nomeCompleto.contains(" ")) return "";
        int espaco = nomeCompleto.indexOf(" ");
        return nomeCompleto.substring(espaco + 1);
    }
}