package br.com.balaio.events.event.application.port;

import java.util.Optional;
import java.util.UUID;

public interface ContatoContaPort {

    Optional<Contato> consultar(UUID conta);

    record Contato(String nome, String email) {
    }
}
