package com.igreja.gestao.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ReconhecimentoFacialService {

    // O endereço do Python que deixamos rodando
    private final String PYTHON_URL = "http://localhost:5000/check";

    public String verificarPresenca(byte[] fotoDaCamera) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Prepara a foto para envio
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("foto", new ByteArrayResource(fotoDaCamera) {
            @Override
            public String getFilename() {
                return "captura.jpg";
            }
        });

        // 2. Configura o cabeçalho HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 3. Envia para o Python e espera a resposta
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_URL, requestEntity, Map.class);
            Map<String, Object> corpoResposta = response.getBody();

            if (corpoResposta != null) {
                String status = (String) corpoResposta.get("status");

                if ("MEMBRO".equals(status)) {
                    String id = (String) corpoResposta.get("id");
                    System.out.println("SUCESSO: Membro identificado com ID " + id);
                    return id; // Retorna o ID do membro
                } else {
                    System.out.println("AVISO: Visitante não identificado.");
                    return "VISITANTE";
                }
            }
        } catch (Exception e) {
            System.err.println("ERRO: Não consegui falar com o Python. Ele está rodando? " + e.getMessage());
        }

        return "ERRO";
    }
}
