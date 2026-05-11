package com.igreja.gestao.repository;

import com.igreja.gestao.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PresencaRepository extends JpaRepository<Presenca, Long> {

    boolean existsByMembroIdAndDataHoraAfter(Long idMembro, LocalDateTime limite);

    boolean existsByTipoAndDataHoraAfter(String tipo, LocalDateTime limite);

    // Filtro para a TV (Placar): Mostra só o que aconteceu nas últimas X horas
    List<Presenca> findByDataHoraAfterOrderByDataHoraDesc(LocalDateTime limite);

    // Filtro para o Histórico/Relatório: Mostra o dia inteiro
    List<Presenca> findAllByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);
}