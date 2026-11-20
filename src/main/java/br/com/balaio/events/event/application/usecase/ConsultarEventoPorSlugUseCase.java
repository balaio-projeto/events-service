package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;

import java.util.Optional;

public class ConsultarEventoPorSlugUseCase {

    private final EventoRepositoryPort port;

    public ConsultarEventoPorSlugUseCase(EventoRepositoryPort port){
        this.port = port;
    }

   public Optional<Evento> executar(String slug){
        return port.buscarEventoPublicoPorSlug(slug);
    }
}
