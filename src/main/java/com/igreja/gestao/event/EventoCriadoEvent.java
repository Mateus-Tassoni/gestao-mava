package com.igreja.gestao.event;
import com.igreja.gestao.model.Evento;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EventoCriadoEvent extends ApplicationEvent {
    private final Evento evento;

    public EventoCriadoEvent(Object source, Evento evento) {
        super(source);
        this.evento = evento;
    }
}