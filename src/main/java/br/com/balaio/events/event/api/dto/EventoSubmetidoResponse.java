package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.Evento;

public record EventoSubmetidoResponse(
        String slug,
        String status
) {

    public static EventoSubmetidoResponse fromDomain(Evento evento) {
        return new EventoSubmetidoResponse(evento.slug(), evento.statusAprovacao().name());
    }
}
