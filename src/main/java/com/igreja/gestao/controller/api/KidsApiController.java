package com.igreja.gestao.controller.api;

import com.igreja.gestao.model.Checkin;
import com.igreja.gestao.model.Crianca;
import com.igreja.gestao.repository.CheckinRepository;
import com.igreja.gestao.repository.CriancaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class KidsApiController {

    @Autowired private CheckinRepository checkinRepository;
    @Autowired private CriancaRepository criancaRepository;

    @PostMapping("/kids/confirmar/{criancaId}")
    public ResponseEntity<?> realizarCheckin(@PathVariable Long criancaId) {

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);

        // A. Verifica duplicidade
        boolean jaFez = checkinRepository.jaFezCheckinHoje(criancaId, inicioDia, fimDia);
        if (jaFez) {
            return ResponseEntity.badRequest().body("Check-in já realizado hoje!");
        }

        // B. Busca a criança
        Crianca crianca = criancaRepository.findById(criancaId)
                .orElseThrow(() -> new RuntimeException("Criança não encontrada"));

        // C. Gera Código Sequencial (001, 002...)
        long qtdHoje = checkinRepository.countByDataHoraBetween(inicioDia, fimDia);
        long proximoNumero = qtdHoje + 1;
        String codigoEtiqueta = String.format("%03d", proximoNumero);

        // D. Cria e Salva
        Checkin novoCheckin = new Checkin();
        novoCheckin.setCrianca(crianca);
        novoCheckin.setCodigoEtiqueta(codigoEtiqueta);
        novoCheckin.setResponsavel(crianca.getResponsavel());
        novoCheckin.setImpresso(false); // Vai pra fila de impressão

        checkinRepository.save(novoCheckin);

        return ResponseEntity.ok().body("Check-in realizado: " + codigoEtiqueta);
    }

    // 2. O MONITOR (PC) CHAMA ISSO A CADA 3 SEGUNDOS
    @GetMapping("/impressao/pendentes")
    public List<Checkin> buscarPendentes() {
        return checkinRepository.findByImpressoFalseOrderByDataHoraAsc();
    }

    // 3. O MONITOR CHAMA ISSO DEPOIS QUE A IMPRESSORA IMPRIME
    @PostMapping("/impressao/confirmar/{checkinId}")
    public void confirmarImpressao(@PathVariable Long checkinId) {
        Checkin checkin = checkinRepository.findById(checkinId).orElse(null);
        if (checkin != null) {
            checkin.setImpresso(true);
            checkinRepository.save(checkin);
        }
    }

    // 4. HISTÓRICO DE HOJE
    @GetMapping("/historico/hoje")
    public List<Checkin> historicoHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);

        // Filtra todos os checkins do dia, ordena do mais novo pro mais antigo
        return checkinRepository.findAll().stream()
                .filter(c -> c.getDataHora().isAfter(inicioDia) && c.getDataHora().isBefore(fimDia))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    // 5. REIMPRIMIR
    @PostMapping("/reimprimir/{id}")
    public ResponseEntity<?> reimprimir(@PathVariable Long id) {
        Checkin c = checkinRepository.findById(id).orElse(null);
        if (c != null) {
            c.setImpresso(false);
            checkinRepository.save(c);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // 6. MÓDULO DE SAÍDA (CHECK-OUT)
    // ==========================================

    // Busca Checkin pelo código da etiqueta (Ex: "005")
    @GetMapping("/checkout/buscar/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {

        // Garante que o código tenha 3 dígitos (se digitar "5", vira "005")
        // Isso evita erro se o voluntário digitar sem zeros
        String codigoFormatado = String.format("%03d", Integer.parseInt(codigo));

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);

        // Busca checkin DE HOJE com esse código
        Optional<Checkin> checkinOpt = checkinRepository.findByCodigoEtiquetaAndDataHoraBetween(codigoFormatado, inicioDia, fimDia);

        if (checkinOpt.isPresent()) {
            Checkin c = checkinOpt.get();

            // Se já saiu, avisa
            if (c.getDataSaida() != null) {
                return ResponseEntity.badRequest().body("Esta criança já saiu hoje às " + c.getDataSaida().toLocalTime().toString().substring(0,5));
            }

            return ResponseEntity.ok(c);
        }

        return ResponseEntity.notFound().build();
    }

    // Confirma a saída (Dar baixa)
    @PostMapping("/checkout/confirmar/{id}")
    public ResponseEntity<?> confirmarSaida(@PathVariable Long id) {
        Checkin c = checkinRepository.findById(id).orElse(null);
        if (c != null) {
            c.setDataSaida(LocalDateTime.now());
            checkinRepository.save(c);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}