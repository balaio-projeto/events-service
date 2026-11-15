package br.com.balaio.events.event.application.port;

import br.com.balaio.events.event.domain.Evento;
import java.util.List;

public interface EventoRepositoryPort {

    List<Evento> buscarEventosPublicos();
}
