package com.igreja.gestao.service;

import com.igreja.gestao.model.EquipeMinisterio;
import com.igreja.gestao.model.EstruturaEvento;
import com.igreja.gestao.model.Evento;
import com.igreja.gestao.repository.EquipeMinisterioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoService {

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private EquipeMinisterioRepository equipeRepository;

    public void notificarEquipes(Evento evento) {
        System.out.println("[NOTIFICACAO] Iniciando disparos para: " + evento.getNome());

        if (evento.getEstrutura() == null || evento.getEstrutura().isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        String dataFormatada = (evento.getDataHoraInicio() != null) ? evento.getDataHoraInicio().format(fmt) : "--/--";

        // 1. Loop nas ESTRUTURAS (O que o evento precisa? Som? Luz?)
        for (EstruturaEvento item : evento.getEstrutura()) {

            // 2. Busca no banco QUEM sao os LIDERES desse ministerio
            List<EquipeMinisterio> lideres = equipeRepository.findByMinisterioAndFuncao(
                    item.getMinisterio(),
                    EquipeMinisterio.FuncaoMinisterio.LIDER
            );

            // 3. Loop nos LIDERES encontrados (Agora sim temos pessoas)
            for (EquipeMinisterio lider : lideres) {

                // Validacao de telefone
                if (lider.getMembro() != null &&
                        lider.getMembro().getTelefone() != null &&
                        !lider.getMembro().getTelefone().isEmpty()) {

                    String nomeLider = lider.getMembro().getNome();
                    String nomeMinisterio = item.getMinisterio().getNome();
                    String telefone = lider.getMembro().getTelefone();
                    String mensagem = String.format(
                            "Ola %s! \n\n" +
                                    "Voce foi escalado como Lider de %s para o evento:\n" +
                                    "EVENTO: %s\n" +
                                    "DATA: %s\n\n" +
                                    "Por favor, organize sua equipe. Deus abencoe!",
                            nomeLider, nomeMinisterio, evento.getNome(), dataFormatada
                    );

                    // ENVIAR
                    whatsAppService.enviarMensagem(telefone, mensagem);
                }
            }
        }
    }
}