package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;

import java.util.List;

public class ConsultarEventosPublicosUseCase {

    private final EventoRepositoryPort repository;

    public ConsultarEventosPublicosUseCase(EventoRepositoryPort repository){
        this.repository = repository;
    }

    public List<Evento> executar(){
        return repository.buscarEventosPublicos();
    }
}