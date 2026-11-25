package br.com.balaio.events.event.domain;

import java.util.UUID;

public record Solicitante(
        UUID id,
        String nome,
        String email
) {

    public static Solicitante somenteIdentificado(UUID id) {
        return new Solicitante(id, null, null);
    }
}
