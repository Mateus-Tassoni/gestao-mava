package com.igreja.gestao.repository;

import com.igreja.gestao.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findTop5ByStatusAndDataHoraInicioAfterOrderByDataHoraInicioAsc(Evento.StatusEvento status, LocalDateTime data);

    List<Evento> findByStatus(Evento.StatusEvento status);

    // --- AJUSTE NA TRAVA DE SEGURANÇA ---
    @Query("SELECT COUNT(e) > 0 FROM Evento e WHERE e.status IN ('APROVADO', 'PENDENTE') " +
            "AND ((e.dataHoraInicio < :fim AND e.dataHoraFim > :inicio))")
    boolean verificarConflito(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}