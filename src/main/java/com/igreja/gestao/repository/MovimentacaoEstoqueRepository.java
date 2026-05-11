package com.igreja.gestao.repository;

import com.igreja.gestao.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    // ALERTA DE VENCIMENTO: Busca produtos que vencem até uma determinada data
    List<MovimentacaoEstoque> findByDataValidadeBeforeOrderByDataValidadeAsc(LocalDate data);

    // HISTÓRICO DA FAMÍLIA: Busca tudo o que uma família específica já recebeu
    List<MovimentacaoEstoque> findByFamiliaAssistidaIdOrderByDataHoraDesc(Long familiaId);

    // AUDITORIA POR PRODUTO: Ver todas as entradas e saídas de um item específico (ex: Arroz)
    List<MovimentacaoEstoque> findByProdutoIdOrderByDataHoraDesc(Long produtoId);

    // RELATÓRIO DE IMPACTO: Soma a quantidade total de itens distribuídos para famílias
    @Query("SELECT SUM(m.quantidade) FROM MovimentacaoEstoque m " +
            "WHERE m.tipo = 'SAIDA' AND m.familiaAssistida IS NOT NULL")
    Integer somarTotalItensDoados();
}