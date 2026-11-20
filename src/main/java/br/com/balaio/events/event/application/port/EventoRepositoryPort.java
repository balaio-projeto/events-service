package br.com.balaio.events.event.application.port;

import br.com.balaio.events.event.domain.Evento;
import java.util.List;
import java.util.Optional;

public interface EventoRepositoryPort {

    List<Evento> buscarEventosPublicos();

    Optional<Evento> buscarEventoPublicoPorSlug(String slug);

    Evento salvar(Evento evento);

    boolean existePorSlug(String slug);
}
