package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.DetalhesEvento;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.Solicitante;

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
        String motivo,
        ContatoResponse contato,
        SolicitanteResponse solicitante
) {

    public static EventoModeracaoResponse fromDomain(Evento evento, Solicitante solicitante) {
        return new EventoModeracaoResponse(
                EventoResponse.fromDomain(evento),
                evento.statusAprovacao().name(),
                evento.isPublicamenteVisivel(),
                evento.organizadorId(),
                evento.submetidoEm(),
                evento.decididoPor(),
                evento.decididoEm(),
                evento.motivoRejeicao(),
                ContatoResponse.fromDomain(evento.detalhes()),
                SolicitanteResponse.fromDomain(solicitante)
        );
    }

    public record ContatoResponse(
            String nome,
            String telefone,
            String email
    ) {

        static ContatoResponse fromDomain(DetalhesEvento detalhes) {
            return new ContatoResponse(
                    detalhes.contatoNome(),
                    detalhes.contatoTelefone(),
                    detalhes.contatoEmail()
            );
        }
    }

    public record SolicitanteResponse(
            UUID id,
            String nome,
            String email
    ) {

        static SolicitanteResponse fromDomain(Solicitante solicitante) {
            if (solicitante == null) {
                return null;
            }
            return new SolicitanteResponse(solicitante.id(), solicitante.nome(), solicitante.email());
        }
    }
}
