package com.igreja.gestao.service;

import com.igreja.gestao.event.EventoCriadoEvent;
import com.igreja.gestao.model.*;
import com.igreja.gestao.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void salvarComLogica(Evento evento) {
        boolean isNovo = (evento.getId() == null);

        // 1. LÓGICA DE TEMPLATE: Aplica a estrutura padrão (Som, Louvor, etc.)
        if (isNovo && evento.getTipoEvento() != null) {
            for (Ministerio m : evento.getTipoEvento().getEstruturaPadrao()) {
                evento.adicionarNaEstrutura(m);
            }
        }

        // 2. SALVAR: Persiste no banco
        eventoRepository.save(evento);
    }

    // 3. LÓGICA DE NOTIFICAÇÃO (Acionada pelo Pastor na Aprovação)
    @Transactional
    public void notificarAprovacao(Evento evento) {
        if (evento.getStatus() == Evento.StatusEvento.APROVADO && !evento.getEstrutura().isEmpty()) {
            eventPublisher.publishEvent(new EventoCriadoEvent(this, evento));
        }
    }

    public List<Evento> listarTodos() { return eventoRepository.findAll(); }
    public void excluir(Long id) { eventoRepository.deleteById(id); }
    public Evento buscarPorId(Long id) { return eventoRepository.findById(id).orElse(null); }
}