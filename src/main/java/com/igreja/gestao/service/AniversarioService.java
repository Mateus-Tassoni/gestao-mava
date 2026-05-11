package com.igreja.gestao.service;

import com.igreja.gestao.model.Membro;
import com.igreja.gestao.repository.MembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AniversarioService {

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private WhatsAppService whatsappService;

    // Roda todo dia pontualmente às 09:00:00 da manhã no fuso de Brasília/SP
    @Scheduled(cron = "0 0 9 * * *", zone = "America/Sao_Paulo")
    public void dispararMensagensDeAniversario() {

        // Força o horário do Brasil, ignorando a hora do servidor/Docker
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        List<Membro> aniversariantes = membroRepository.findAniversariantesDoDia(hoje.getMonthValue(), hoje.getDayOfMonth());

        if (aniversariantes.isEmpty()) {
            System.out.println("[ANIVERSARIOS] Nenhum membro ativo celebrando hoje (" + hoje + ")");
            return;
        }

        System.out.println("[ANIVERSARIOS] Iniciando envios do dia: " + hoje);

        for (Membro membro : aniversariantes) {

            String numeroDestino = membro.getTelefone();

            if (numeroDestino == null || numeroDestino.trim().isEmpty()) {
                System.out.println("   -> Ignorado: " + membro.getNome() + " (Sem telefone)");
                continue;
            }

            // Pega apenas o primeiro nome para a mensagem ficar mais íntima/humana
            String nomeCompleto = membro.getNome().trim();
            String primeiroNome = nomeCompleto.split(" ")[0];

            String mensagem = String.format(
                    "🎉 Feliz Aniversário, *%s*! 🎂\n\n" +
                            "A Mava se alegra pela sua vida neste dia tão especial.\n" +
                            "Que Deus continue derramando ricas bênçãos sobre você e sua família! 🙌\n\n" +
                            "Um grande abraço de toda a nossa equipe pastoral.",
                    primeiroNome
            );

            try {
                whatsappService.enviarMensagem(numeroDestino, mensagem);
                System.out.println("   -> ✅ Enviado para: " + primeiroNome);

                // Dorme 3 segundos para evitar bloqueio anti-spam do WhatsApp
                Thread.sleep(3000);

            } catch (Exception e) {
                System.out.println("   -> ❌ Erro ao enviar para " + primeiroNome + ": " + e.getMessage());
            }
        }

        System.out.println("[ANIVERSARIOS] Disparos concluidos.");
    }
}