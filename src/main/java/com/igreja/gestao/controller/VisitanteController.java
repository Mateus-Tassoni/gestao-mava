package com.igreja.gestao.controller;

import com.igreja.gestao.model.Visitante;
import com.igreja.gestao.repository.VisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/visitantes")
@PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPCAO')")
public class VisitanteController {

    @Autowired
    private VisitanteRepository repository;

    // --- ROTA: LISTAGEM DE HISTÓRICO ---
    @GetMapping
    public String listar(@org.springframework.web.bind.annotation.RequestParam(value = "filtroCivil", required = false) Visitante.EstadoCivil filtroCivil, Model model) {

        if (filtroCivil != null) {
            // Se o usuário escolheu um filtro, busca só aqueles
            model.addAttribute("visitantes", repository.findByEstadoCivilOrderByNomeAsc(filtroCivil));
            model.addAttribute("filtroAtual", filtroCivil); // Avisa a tela qual foi selecionado
        } else {
            // Se não tem filtro, traz todo mundo
            model.addAttribute("visitantes", repository.findAllByOrderByNomeAsc());
            model.addAttribute("filtroAtual", null);
        }

        // Manda a lista de estados civis para o HTML montar a caixinha de seleção
        model.addAttribute("estadosCivis", Visitante.EstadoCivil.values());

        return "visitantes/lista";
    }

    // --- ROTA: FORMULÁRIO DE NOVO CADASTRO ---
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("visitante", new Visitante());
        model.addAttribute("estadosCivis", Visitante.EstadoCivil.values());
        model.addAttribute("sexos", Visitante.Sexo.values());
        model.addAttribute("congregacoes", Visitante.Congregacao.values());
        return "visitantes/formulario";
    }

    // --- NOVA ROTA: EDITAR ---
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Visitante visitante = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido"));
        model.addAttribute("visitante", visitante);
        model.addAttribute("estadosCivis", Visitante.EstadoCivil.values());
        model.addAttribute("sexos", Visitante.Sexo.values());
        model.addAttribute("congregacoes", Visitante.Congregacao.values());
        return "visitantes/formulario";
    }

    // --- NOVA ROTA: EXCLUIR ---
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/visitantes?excluido";
    }

    // --- ROTA: SALVAR (CRIAR OU ATUALIZAR) ---
    @PostMapping("/salvar")
    public String salvar(Visitante visitante) {
        // 1. Verifica se o ID é nulo ANTES de salvar
        boolean isNovo = (visitante.getId() == null);
        System.out.println(">>> DEBUG: Iniciando salvamento. É novo cadastro? " + isNovo);

        repository.save(visitante);
        System.out.println(">>> DEBUG: Visitante salvo com ID: " + visitante.getId());

        if (isNovo) {
            System.out.println(">>> DEBUG: Disparando rotinas de WhatsApp...");
            rotearParaLideres(visitante);
            enviarBoasVindasVisitante(visitante.getTelefone());
        } else {
            System.out.println(">>> DEBUG: Edição detectada. WhatsApp NÃO será disparado.");
        }

        return "redirect:/visitantes?sucesso";
    }

    // --- LÓGICA DE AVISO AOS LÍDERES (ATUALIZADA) ---
    private void rotearParaLideres(Visitante v) {
        if (v.getDataNascimento() == null) return;

        int idade = Period.between(v.getDataNascimento(), LocalDate.now()).getYears();
        Visitante.EstadoCivil civil = v.getEstadoCivil();
        Visitante.Sexo sexo = v.getSexo();

        // Mulheres Com Propósito
        if (sexo == Visitante.Sexo.FEMININO) {
            enviarAlertaLider("5511974440062", "Carina", v, idade, "Mulheres Com Propósito");
        }

        // Adolescentes e Jovens
        if (civil == Visitante.EstadoCivil.SOLTEIRO) {
            if (idade >= 10 && idade <= 15) {
                enviarAlertaLider("5518991412681", "Ligiane", v, idade, "Adolescentes");
            } else if (idade > 15 && idade <= 30) {
                enviarAlertaLider("5511942202518", "Richard", v, idade, "Jovens");
            }
        }

        // Ministério de Casais (Envia para Vivian e Marcos)
        if (civil == Visitante.EstadoCivil.CASADO) {
            enviarAlertaLider("5511971473707", "Vivian", v, idade, "Casais");
            enviarAlertaLider("5511987195596", "Marcos", v, idade, "Casais");
        }
    }

    private void enviarAlertaLider(String telefoneLider, String nomeLider, Visitante v, int idade, String ministerio) {
        String chatId = telefoneLider + "@c.us";
        String mensagem = "🚨 *Novo Visitante no Radar!*\n\n" +
                "Olá " + nomeLider + "! O sistema direcionou este perfil para o seu ministério (*" + ministerio + "*).\n\n" +
                "👤 *Nome:* " + v.getNome() + "\n" +
                "🎂 *Idade:* " + idade + " anos\n" +
                "💍 *Estado Civil:* " + v.getEstadoCivil() + "\n" +
                "⛪ *Igreja:* " + v.getCongregacao() + "\n" +
                "📱 *WhatsApp:* " + v.getTelefone() + "\n\n" +
                "Dê as boas-vindas e conecte essa pessoa com a gente! 🙏";

        dispararWaha(chatId, mensagem);
    }

    // --- LÓGICA DE BOAS-VINDAS AO VISITANTE ---
    private void enviarBoasVindasVisitante(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) return;

        String numeroLimpo = telefone.replaceAll("[^0-9]", "");
        if (!numeroLimpo.startsWith("55")) {
            numeroLimpo = "55" + numeroLimpo;
        }
        String chatId = numeroLimpo + "@c.us";

        String mensagem1 = "🌿 *Graça e Paz!*\n" +
                "Somos a Familia MAVA e estamos entrando em contato com você porque ficamos muito felizes com a sua visita!\n" +
                "Foi uma grande bênção receber você em nossa igreja e queremos que saiba que você é muito bem-vindo a se juntar à nossa família MAVA! 💚\n\n" +
                "Gostaríamos de te convidar a conhecer mais sobre nossa programação e caminhar ainda mais perto de Jesus junto conosco.";

        String mensagem2 = "Pai amado,\n" +
                "eu coloco essa vida diante de Ti agora.\n" +
                "O Senhor conhece o coração, conhece sua história e tudo aquilo que ela tem vivido.\n" +
                "Visita, Senhor, cada área da sua vida —\n" +
                "onde há dor, traz cura…\n" +
                "onde há medo, traz paz…\n" +
                "onde há dúvida, traz direção.\n" +
                "Que ela sinta agora o Teu amor de forma real,\n" +
                "abraçando, acolhendo e trazendo descanso.\n" +
                "Abençoa seus caminhos, sua casa e tudo aquilo que está em suas mãos.\n" +
                "E que, a partir de hoje, ela experimente um novo tempo contigo.\n" +
                "Oramos em nome de Jesus. Amém 🙏";

        dispararWaha(chatId, mensagem1);
        dispararWaha(chatId, mensagem2);
    }

    // --- MOTOR DE ENVIO DO WAHA ---
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

            } catch (Exception e) {
                System.out.println(">>> WAHA ERRO NO ENVIO: " + e.getMessage());
            }
        }).start();
    }
}