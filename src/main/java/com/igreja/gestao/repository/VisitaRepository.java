package com.igreja.gestao.repository;

import com.igreja.gestao.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {
    // Conta as visitas que vieram do QR Code e estão PENDENTES
    long countByStatus(Visita.StatusVisita status);
}