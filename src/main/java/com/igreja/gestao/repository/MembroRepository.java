package com.igreja.gestao.repository;

import com.igreja.gestao.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {

    // --- TRAVAS DE DUPLICIDADE (NOVOS) ---
    boolean existsByCpf(String cpf);
    boolean existsByTelefone(String telefone);

    // --- ORDENAÇÃO FORÇADA E ABSOLUTA (Ignora maiúsculas, minúsculas e espaços ocultos) ---
    @Query("SELECT m FROM Membro m ORDER BY LOWER(TRIM(m.nome)) ASC")
    List<Membro> buscarTodosOrdemAlfabeticaForcada();

    @Query("SELECT m FROM Membro m WHERE LOWER(m.nome) LIKE LOWER(CONCAT('%', :busca, '%')) ORDER BY LOWER(TRIM(m.nome)) ASC")
    List<Membro> buscarPorNomeOrdemAlfabeticaForcada(@Param("busca") String busca);

    // Busca membros por status
    @Query("SELECT m FROM Membro m WHERE m.status = :status ORDER BY LOWER(TRIM(m.nome)) ASC")
    List<Membro> buscarPorStatusOrdemAlfabeticaForcada(@Param("status") Membro.StatusMembro status);

    // Busca aniversariantes do mês atual
    @Query("SELECT m FROM Membro m WHERE MONTH(m.dataNascimento) = MONTH(CURRENT_DATE) ORDER BY DAY(m.dataNascimento) ASC, LOWER(TRIM(m.nome)) ASC")
    List<Membro> findAniversariantesDoMes();

    // BUSCA OS ANIVERSARIANTES EXATOS DO DIA (APENAS ATIVOS)
    @Query("SELECT m FROM Membro m WHERE MONTH(m.dataNascimento) = :mes AND DAY(m.dataNascimento) = :dia AND m.status = 'ATIVO'")
    List<Membro> findAniversariantesDoDia(@Param("mes") int mes, @Param("dia") int dia);
}