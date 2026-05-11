package com.igreja.gestao.repository;

import com.igreja.gestao.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    // --- O MÉTODO NOVO PARA O FILTRO DE MÊS/ANO DA TESOURARIA ---
    List<Lancamento> findByDataBetweenOrderByDataDesc(LocalDate inicio, LocalDate fim);

    // Soma Dízimos, Ofertas e Doações (ENTRADAS - Geral)
    @Query("SELECT SUM(l.valor) FROM Lancamento l WHERE l.tipo IN ('DIZIMO', 'OFERTA', 'DOACAO')")
    BigDecimal calcularTotalEntradas();

    // Soma tudo que NÃO for entrada (SAÍDAS - Geral)
    @Query("SELECT SUM(l.valor) FROM Lancamento l WHERE l.tipo NOT IN ('DIZIMO', 'OFERTA', 'DOACAO')")
    BigDecimal calcularTotalSaidas();

    // CONTA AS SAÍDAS DENTRO DE UM PERÍODO (Para Dashboard/Agenda)
    @Query("SELECT COUNT(l) FROM Lancamento l WHERE l.tipo NOT IN ('DIZIMO', 'OFERTA', 'DOACAO') AND l.data BETWEEN :inicioSemana AND :fimSemana")
    Long contarContasAPagarNaSemana(@Param("inicioSemana") LocalDate inicioSemana, @Param("fimSemana") LocalDate fimSemana);
}