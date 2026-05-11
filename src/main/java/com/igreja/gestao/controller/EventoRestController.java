package com.igreja.gestao.controller;

import com.igreja.gestao.model.Evento;
import com.igreja.gestao.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
public class EventoRestController {

    @Autowired
    private EventoRepository eventRepository;

    @GetMapping
    public List<Map<String, Object>> listarEventosParaCalendario() {
        List<Evento> eventos = eventRepository.findAll();
        List<Map<String, Object>> eventosJson = new ArrayList<>();

        for (Evento e : eventos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            String statusStr = (e.getStatus() != null) ? e.getStatus().toString() : "PENDENTE";

            // Título com ícone visual
            String icone = statusStr.equals("PENDENTE") ? "⏳ " : "✓ ";
            map.put("title", icone + e.getNome());

            map.put("start", e.getDataHoraInicio().toString());
            if (e.getDataHoraFim() != null) {
                map.put("end", e.getDataHoraFim().toString());
            }

            // Manda o status para o JS pintar o bloco com a cor certa
            map.put("status", statusStr);
            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("status", statusStr);
            map.put("extendedProps", extendedProps);

            eventosJson.add(map);
        }
        return eventosJson;
    }
}