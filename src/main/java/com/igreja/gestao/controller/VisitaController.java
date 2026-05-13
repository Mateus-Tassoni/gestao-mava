package com.igreja.gestao.controller;

import com.igreja.gestao.model.Membro;
import com.igreja.gestao.model.Visita;
import com.igreja.gestao.repository.MembroRepository;
import com.igreja.gestao.repository.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/visitas")
public class VisitaController {

    @Autowired
    private VisitaRepository visitaRepository;

    @Autowired
    private MembroRepository membroRepository;

    // --- 1. TELA PRINCIPAL (LISTA + CALENDÁRIO) ---
    @GetMapping
    public String listarVisitas(Model model) {
        model.addAttribute("visitas", visitaRepository.findAll());
        return "visitas/lista";
    }

    // --- 2. FORMULÁRIO (NOVO) ---
    @GetMapping("/novo")
    public String novaVisita(Model model) {
        model.addAttribute("visita", new Visita());
        model.addAttribute("listaMembros", membroRepository.findAll());
        return "visitas/formulario";
    }

    // --- 3. FORMULÁRIO (EDITAR) ---
    @GetMapping("/editar/{id}")
    public String editarVisita(@PathVariable Long id, Model model) {
        Visita visita = visitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));

        model.addAttribute("visita", visita);
        model.addAttribute("listaMembros", membroRepository.findAll());
        return "visitas/formulario";
    }

    // --- 4. EXCLUIR ---
    @GetMapping("/excluir/{id}")
    public String excluirVisita(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            visitaRepository.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Visita excluída com sucesso.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao excluir a visita.");
        }
        return "redirect:/visitas";
    }

    // --- 5. SALVAR E DISPARAR WHATSAPP (AGORA COM O MOTOR WAHA DEFINITIVO) ---
    @PostMapping("/salvar")
    public String salvar(Visita visita, RedirectAttributes attributes) {

        boolean isNovo = (visita.getId() == null);
        boolean dispararNotificacao = false;

        // 1. LÓGICA PARA VISITA NOVA: Se já nasce com responsável, notifica!
        if (isNovo && visita.getResponsavel() != null) {
            dispararNotificacao = true;
        }

        // 2. LÓGICA PARA EDIÇÃO: Se antes era null e agora tem alguém, notifica!
        else if (!isNovo) {
            Visita visitaAntiga = visitaRepository.findById(visita.getId()).orElse(new Visita());
            if (visitaAntiga.getResponsavel() == null && visita.getResponsavel() != null) {
                dispararNotificacao = true;
            }
        }

        // Regras de limpeza de campos
        if (visita.getMembro() != null && visita.getMembro().getId() != null) {
            visita.setNomeNaoMembro(null);
        } else {
            visita.setMembro(null);
        }

        if(visita.getStatus() == null) {
            visita.setStatus(Visita.StatusVisita.AGENDADA);
        }

        visitaRepository.save(visita);

        // Log de Debug para você ver no console se o sistema entendeu que deve notificar
        System.out.println(">>> DEBUG VISITA: Salvo com Sucesso. Notificar líder? " + dispararNotificacao);

        if (dispararNotificacao && visita.getResponsavel() != null) {
            String nomePessoa = (visita.getMembro() != null) ? visita.getMembro().getNome() : visita.getNomeNaoMembro();
            String tipoVisitaTexto = (visita.getTipo() != null) ? visita.getTipo().name() : "Não especificado";
            String telefonePessoa = (visita.getTelefoneContato() != null && !visita.getTelefoneContato().isEmpty()) ? visita.getTelefoneContato() : "Não informado";

            String telefoneLider = visita.getResponsavel().getTelefone();

            if (telefoneLider != null && !telefoneLider.trim().isEmpty()) {
                String mensagem = "🚨 *NOVA VISITA DESIGNADA*\n\n" +
                        "Olá *" + visita.getResponsavel().getNome() + "*!\n" +
                        "A secretaria direcionou uma nova visita/aconselhamento pastoral para você:\n\n" +
                        "👤 *Pessoa/Família:* " + nomePessoa + "\n" +
                        "🏷️ *Motivo:* " + tipoVisitaTexto + "\n" +
                        "📱 *Telefone/Contato:* " + telefonePessoa + "\n\n" +
                        "Acesse o sistema para ver mais detalhes. Deus abençoe! 🙏";

                String numeroLimpo = telefoneLider.replaceAll("[^0-9]", "");
                if (!numeroLimpo.startsWith("55")) numeroLimpo = "55" + numeroLimpo;
                String chatId = numeroLimpo + "@c.us";

                System.out.println(">>> DEBUG VISITA: Preparando envio para WAHA... ChatId: " + chatId);
                dispararWaha(chatId, mensagem);

                attributes.addFlashAttribute("mensagemSucesso", "Visita salva e líder notificado!");
            }
        } else {
            attributes.addFlashAttribute("mensagemSucesso", "Registro de visita salvo com sucesso!");
        }

        return "redirect:/visitas";
    }

    // --- MOTOR DE ENVIO DO WAHA (COPIADO DO VISITANTE_CONTROLLER) ---
    private void dispararWaha(String chatId, String mensagem) {
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String wahaUrl = "http://192.168.0.7:8081/api/sendText";

                Map<String, Object> payload = new HashMap<>();
                payload.put("chatId", chatId);
                payload.put("text", mensagem);
                payload.put("session", "default");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Api-Key", "minhasenha123");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity(wahaUrl, request, String.class);

                System.out.println(">>> WAHA VISITAS ENVIO OK PARA: " + chatId);

            } catch (Exception e) {
                System.out.println(">>> WAHA ERRO NO ENVIO (VISITAS): " + e.getMessage());
            }
        }).start();
    }

    // --- 6. API PARA ALIMENTAR O FULLCALENDAR ---
    @GetMapping("/api/visitas")
    @ResponseBody
    public List<Map<String, Object>> listarVisitasParaCalendario() {
        return visitaRepository.findAll().stream().map(visita -> {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", visita.getId());

            String nome = (visita.getMembro() != null) ? visita.getMembro().getNome() : visita.getNomeNaoMembro();
            evento.put("title", "Visita: " + nome);

            if (visita.getDataVisita() != null) {
                evento.put("start", visita.getDataVisita().toString());
            }

            String cor;
            if (visita.getStatus() == Visita.StatusVisita.REALIZADA) {
                cor = "#198754";
            } else if (visita.getStatus() == Visita.StatusVisita.CANCELADA) {
                cor = "#dc3545";
            } else {
                cor = "#fd7e14";
            }

            evento.put("backgroundColor", cor);
            evento.put("borderColor", cor);
            evento.put("url", "/visitas/editar/" + visita.getId());

            return evento;
        }).collect(Collectors.toList());
    }
}