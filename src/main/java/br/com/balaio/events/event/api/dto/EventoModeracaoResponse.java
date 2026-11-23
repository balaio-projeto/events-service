package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.Evento;

import java.time.Instant;
import java.util.UUID;

public record EventoModeracaoResponse(
        EventoResponse evento,
        String status,
        boolean publicado,
        UUID organizadorId,
        Instant submetidoEm,
        UUID decididoPor,
        Instant decididoEm,
        String motivo
) {

    public static EventoModeracaoResponse fromDomain(Evento evento) {
        return new EventoModeracaoResponse(
                EventoResponse.fromDomain(evento),
                evento.statusAprovacao().name(),
                evento.isPublicamenteVisivel(),
                evento.organizadorId(),
                evento.submetidoEm(),
                evento.decididoPor(),
                evento.decididoEm(),
                evento.motivoRejeicao()
        );
    }
}
