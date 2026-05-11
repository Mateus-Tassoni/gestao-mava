package com.igreja.gestao.listener;

import com.igreja.gestao.event.EventoCriadoEvent;
import com.igreja.gestao.model.EquipeMinisterio;
import com.igreja.gestao.model.EstruturaEvento;
import com.igreja.gestao.model.Evento;
import com.igreja.gestao.repository.EquipeMinisterioRepository;
import com.igreja.gestao.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NotificacaoListener {

    @Autowired
    private EquipeMinisterioRepository equipeRepository;

    @Autowired
    private WhatsAppService whatsappService;

    @Async
    @EventListener
    public void handleEventoCriado(EventoCriadoEvent event) {
        Evento evento = event.getEvento();

        System.out.println("[NOTIFICACAO] Processando aprovacao de evento: " + evento.getNome());

        // Se não tem estrutura (equipes), não tem pra quem mandar mensagem
        if (evento.getEstrutura() == null || evento.getEstrutura().isEmpty()) {
            return;
        }

        // 1. Varre a lista de estruturas do evento
        for (EstruturaEvento item : evento.getEstrutura()) {

            // 2. Busca LIDERES usando a sua lógica original (via Repository)
            if (item.getMinisterio() != null) {
                List<EquipeMinisterio> lideres = equipeRepository.findByMinisterioAndFuncao(
                        item.getMinisterio(),
                        EquipeMinisterio.FuncaoMinisterio.LIDER
                );

                // 3. Envia mensagem para cada líder encontrado
                for (EquipeMinisterio lider : lideres) {
                    if (lider.getMembro() != null
                            && lider.getMembro().getTelefone() != null
                            && !lider.getMembro().getTelefone().isEmpty()) {

                        String texto = montarTextoMensagem(lider.getMembro().getNome(), evento, item.getMinisterio().getNome());
                        whatsappService.enviarMensagem(lider.getMembro().getTelefone(), texto);

                        System.out.println("   -> Enviado para: " + lider.getMembro().getNome());
                    }
                }
            }
        }
    }

    private String montarTextoMensagem(String nomeLider, Evento evento, String ministerio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM 'as' HH:mm");
        String dataFormatada = evento.getDataHoraInicio() != null ? evento.getDataHoraInicio().format(formatter) : "A definir";

        return String.format(
                "Ola *%s*! A Paz. \n\n" +
                        "✅ *AGENDA APROVADA* \n" +
                        "O Ministerio de *%s* foi escalado.\n\n" +
                        "📅 Evento: *%s*\n" +
                        "⏰ Data: %s\n" +
                        "📍 Local: %s\n\n" +
                        "Por favor, acesse o sistema para organizar a escala.",
                nomeLider,
                ministerio,
                evento.getNome(),
                dataFormatada,
                evento.getLocal()
        );
    }
}