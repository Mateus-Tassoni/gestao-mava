package com.igreja.gestao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.key}")
    private String apiKey;

    @Value("${whatsapp.instance}")
    private String sessionName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarMensagem(String telefone, String texto) {
        if (telefone == null || telefone.trim().isEmpty()) {
            System.out.println("[ALERTA] Telefone nao informado para envio de WhatsApp.");
            return;
        }

        try {
            // 1. Limpar o Telefone (Remove traços, parênteses e espaços)
            String numeroLimpo = telefone.replaceAll("[^0-9]", "");

            // Se o usuário digitou sem o 55 (DDI do Brasil), adiciona
            if (numeroLimpo.length() == 10 || numeroLimpo.length() == 11) {
                numeroLimpo = "55" + numeroLimpo;
            }

            // 2. Montar o JSON para a API
            Map<String, String> body = new HashMap<>();
            body.put("chatId", numeroLimpo + "@c.us");
            body.put("text", texto);
            body.put("session", sessionName);

            // 3. Montar o Cabeçalho com a Senha (API Key)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", apiKey);

            // 4. Disparar a requisição
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(apiUrl, request, String.class);

            System.out.println("[SUCESSO] WhatsApp enviado para: " + numeroLimpo);

        } catch (Exception e) {
            // O sistema engole o erro da API, avisa de forma simples e não quebra.
            System.err.println("[AVISO] Falha silenciosa na API do WhatsApp. O evento continua rodando. Motivo: " + e.getMessage());
        }
    }
}