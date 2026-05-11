package com.igreja.gestao.repository;

import com.igreja.gestao.model.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    // 1. Conta quantos check-ins ocorreram hoje (Para gerar o Nº Sequencial: 001, 002...)
    long countByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    // 2. Busca a fila de impressão (Só os que ainda não foram impressos)
    List<Checkin> findByImpressoFalseOrderByDataHoraAsc();
    Optional<Checkin> findByCodigoEtiquetaAndDataHoraBetween(String codigo, LocalDateTime inicio, LocalDateTime fim);

    // 3. Segurança : Verifica se a criança JÁ ESTÁ no sistema (Check-in sem Check-out)
    @Query("SELECT COUNT(c) > 0 FROM Checkin c WHERE c.crianca.id = :criancaId AND c.dataHora BETWEEN :inicio AND :fim AND c.dataSaida IS NULL")
    boolean jaFezCheckinHoje(@Param("criancaId") Long criancaId,
                             @Param("inicio") LocalDateTime inicio,
                             @Param("fim") LocalDateTime fim);
}