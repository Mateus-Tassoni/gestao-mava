package com.igreja.gestao.repository;

import com.igreja.gestao.model.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamiliaRepository extends JpaRepository<Familia, Long> {
    List<Familia> findByNomeRepresentanteContainingIgnoreCase(String nome);
}