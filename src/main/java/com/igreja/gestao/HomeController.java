package com.igreja.gestao.controller;

import com.igreja.gestao.model.*;
import com.igreja.gestao.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired private MembroRepository membroRepository;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private VisitaRepository visitaRepository; // <-- Injetado para o alerta do QR Code

    @GetMapping("/")
    public String entradaPrincipal(HttpServletRequest request) {
        String dominio = request.getServerName();

        if (dominio != null) {
            if (dominio.contains("kids") || dominio.contains("checkin")) {
                return "redirect:/kids/checkin";
            }
            if (dominio.contains("sysmava")) {
                return "redirect:/dashboard";
            }
        }
        return "redirect:/dashboard";
    }

    @GetMapping({"/dashboard", "/home"})
    public String painelAdministrativo(Model model) {

        // --- 1. DADOS GERAIS ---
        long totalMembros = membroRepository.count();
        model.addAttribute("qtdMembros", totalMembros);

        // --- 2. AGENDA ---
        List<Evento> eventosAprovados = eventoRepository.findTop5ByStatusAndDataHoraInicioAfterOrderByDataHoraInicioAsc(
                Evento.StatusEvento.APROVADO, LocalDateTime.now());
        model.addAttribute("proximosEventos", eventosAprovados);

        // --- 3. ALERTAS DE EVENTOS ---
        List<Evento> eventosPendentes = eventoRepository.findByStatus(Evento.StatusEvento.PENDENTE);
        model.addAttribute("eventosPendentes", eventosPendentes);

        // --- 4. FINANCEIRO (SALDO E ALERTAS) ---
        BigDecimal totalEntradas = lancamentoRepository.calcularTotalEntradas();
        BigDecimal totalSaidas = lancamentoRepository.calcularTotalSaidas();

        if (totalEntradas == null) totalEntradas = BigDecimal.ZERO;
        if (totalSaidas == null) totalSaidas = BigDecimal.ZERO;

        model.addAttribute("saldoCaixa", totalEntradas.subtract(totalSaidas));

        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Long contasVencendoSemana = lancamentoRepository.contarContasAPagarNaSemana(inicioSemana, fimSemana);
        model.addAttribute("contasVencendoSemana", contasVencendoSemana != null ? contasVencendoSemana : 0L);

        // --- 5. ESTOQUE (QUANTIDADE CRÍTICA) ---
        List<Produto> produtosBaixos = produtoRepository.findProdutosAbaixoDoMinimo();
        model.addAttribute("produtosEmBaixa", produtosBaixos);

        // --- 6. ALERTAS DE VALIDADE NO ESTOQUE ---
        LocalDate trintaDiasFrente = LocalDate.now().plusDays(30);

        List<MovimentacaoEstoque> lotesVencendo = movimentacaoRepository.findAll().stream()
                .filter(m -> (m.getTipo() == TipoMovimentacao.ENTRADA || m.getTipo() == TipoMovimentacao.DOACAO))
                .filter(m -> m.getDataValidade() != null)
                .filter(m -> m.getDataValidade().isBefore(trintaDiasFrente) || m.getDataValidade().isEqual(trintaDiasFrente))
                .collect(Collectors.toList());

        model.addAttribute("lotesVencendo", lotesVencendo);

        // --- 7. ALERTA DE CUIDADO PASTORAL (QR CODE) ---
        // Contamos quantas visitas estão com status PENDENTE no banco
        long visitasPendentes = visitaRepository.countByStatus(Visita.StatusVisita.PENDENTE);
        model.addAttribute("visitasPendentes", visitasPendentes);

        return "home";
    }
}