package com.igreja.gestao.repository;

import com.igreja.gestao.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    // Busca todos os visitantes em ordem alfabética
    List<Visitante> findAllByOrderByNomeAsc();

    // Busca por nome (Filtro da listagem) ignorando maiúsculas e minúsculas
    List<Visitante> findByNomeContainingIgnoreCaseOrderByNomeAsc(String busca);
    // Filtra por Estado Civil
    List<Visitante> findByEstadoCivilOrderByNomeAsc(Visitante.EstadoCivil estadoCivil);

}