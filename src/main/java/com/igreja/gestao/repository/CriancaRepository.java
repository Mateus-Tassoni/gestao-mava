package com.igreja.gestao.repository;

import com.igreja.gestao.model.Crianca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CriancaRepository extends JpaRepository<Crianca, Long> {

    @Query("SELECT c FROM Crianca c WHERE REPLACE(REPLACE(REPLACE(REPLACE(c.responsavel.telefone, '(', ''), ')', ''), '-', ''), ' ', '') = :telefoneLimpo")
    List<Crianca> buscarPorTelefoneLimpo(@Param("telefoneLimpo") String telefoneLimpo);
}