package com.igreja.gestao.repository;

import com.igreja.gestao.model.EquipeMinisterio;
import com.igreja.gestao.model.Ministerio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipeMinisterioRepository extends JpaRepository<EquipeMinisterio, Long> {

    List<EquipeMinisterio> findByMinisterioAndFuncao(
            Ministerio ministerio,
            EquipeMinisterio.FuncaoMinisterio funcao
    );
}