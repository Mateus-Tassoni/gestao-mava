package com.igreja.gestao.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Ministerio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome; // "Som", "Louvor"

    @OneToMany(mappedBy = "ministerio", fetch = FetchType.EAGER)
    private List<EquipeMinisterio> membros = new ArrayList<>();
}