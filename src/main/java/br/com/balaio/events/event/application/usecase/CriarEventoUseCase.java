package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CriarEventoUseCase {

    private final EventoRepositoryPort repository;
    private final Clock relogio;

    public CriarEventoUseCase(EventoRepositoryPort repository, Clock relogio){
        this.repository = repository;
        this.relogio = relogio;
    }

    public Evento executar(Evento entrada, UUID contaOrganizadora) {

        if (repository.existePorSlug(entrada.slug())) {
            throw new EventoJaExistenteException();
        }

        return repository.salvar(entrada.submetidoPor(contaOrganizadora, relogio.instant().truncatedTo(ChronoUnit.MICROS)));
    }
}
