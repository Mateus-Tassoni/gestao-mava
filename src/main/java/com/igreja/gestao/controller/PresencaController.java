package com.igreja.gestao.controller;

import com.igreja.gestao.model.Membro;
import com.igreja.gestao.model.Presenca;
import com.igreja.gestao.repository.MembroRepository;
import com.igreja.gestao.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/presenca")
public class PresencaController {

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private MembroRepository membroRepository;

    // Cache temporário para o tracking ID da IA (opcional)
    private Map<Integer, LocalDateTime> visitantesRastreados = new HashMap<>();

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarPresenca(@RequestBody DadosPresenca dados) {
        Membro membroEncontrado = null;

        // Regra: Não registra a mesma pessoa/visitante se já entrou nos últimos 30 minutos
        LocalDateTime limiteTrava = LocalDateTime.now().minusMinutes(30);

        if ("MEMBRO".equals(dados.tipo) && dados.id != null) {
            try {
                Long idMembro = Long.parseLong(dados.id);
                if (presencaRepository.existsByMembroIdAndDataHoraAfter(idMembro, limiteTrava)) {
                    return ResponseEntity.ok("Membro já registrado recentemente.");
                }
                membroEncontrado = membroRepository.findById(idMembro).orElse(null);
            } catch (NumberFormatException e) { return ResponseEntity.badRequest().body("ID Inválido"); }
        }
        else if ("VISITANTE".equals(dados.tipo)) {
            if (presencaRepository.existsByTipoAndDataHoraAfter("VISITANTE", limiteTrava)) {
                return ResponseEntity.ok("Visitante já registrado recentemente.");
            }
        }

        Presenca novaPresenca = new Presenca(membroEncontrado, dados.tipo);
        presencaRepository.save(novaPresenca);
        return ResponseEntity.ok("Presença registrada!");
    }

    @GetMapping("/placar")
    public ResponseEntity<PlacarDTO> getPlacar() {
        // REGRA DE NEGÓCIO: A TV limpa a cada 5 horas
        LocalDateTime limiteCincoHoras = LocalDateTime.now().minusHours(5);
        List<Presenca> recentes = presencaRepository.findByDataHoraAfterOrderByDataHoraDesc(limiteCincoHoras);

        PlacarDTO p = new PlacarDTO();
        p.totalMembros = recentes.stream().filter(x -> "MEMBRO".equals(x.getTipo())).count();
        p.totalVisitantes = recentes.stream().filter(x -> "VISITANTE".equals(x.getTipo())).count();
        Presenca ultima = recentes.stream().findFirst().orElse(null);

        if (ultima != null) {
            if (ultima.getMembro() != null) {
                p.ultimoNome = ultima.getMembro().getNome();
                p.ultimoCargo = ultima.getMembro().getCargo().toString();
                p.fotoUrl = "/uploads/" + ultima.getMembro().getFotoUrl();
            } else {
                p.ultimoNome = "Visitante";
                p.ultimoCargo = "Seja bem-vindo!";
                p.fotoUrl = "/img/visitante.png";
            }
            p.ultimoTipo = ultima.getTipo();
            p.horario = ultima.getDataHora().toString();
        } else {
            p.ultimoNome = "Aguardando...";
            p.ultimoCargo = "---";
            p.ultimoTipo = "---";
            p.fotoUrl = "/img/logo.png";
        }
        return ResponseEntity.ok(p);
    }

    @GetMapping("/historico")
    public ModelAndView exibirHistorico() {
        ModelAndView mv = new ModelAndView("presenca/historico");
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        List<Presenca> listaHoje = presencaRepository.findAllByDataHoraBetweenOrderByDataHoraDesc(inicioDia, fimDia);

        mv.addObject("historicoMembros", listaHoje.stream().filter(p -> "MEMBRO".equals(p.getTipo())).toList());
        mv.addObject("historicoVisitantes", listaHoje.stream().filter(p -> "VISITANTE".equals(p.getTipo())).toList());
        mv.addObject("qtdTotal", listaHoje.size());

        return mv;
    }

    public static class DadosPresenca {
        public String tipo;
        public String id;
        public Integer trackingId;
    }

    public static class PlacarDTO {
        public long totalMembros;
        public long totalVisitantes;
        public String ultimoNome;
        public String ultimoCargo;
        public String ultimoTipo;
        public String horario;
        public String fotoUrl;
    }
}