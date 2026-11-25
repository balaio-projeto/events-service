package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public class CriarEventoUseCase {

    private final EventoRepositoryPort repository;
    private final Clock relogio;

    public CriarEventoUseCase(EventoRepositoryPort repository, Clock relogio){
        this.repository = repository;
        this.relogio = relogio;
    }

    public Evento executar(Evento entrada, UUID contaOrganizadora) {

        Optional<Evento> existente = repository.buscarPorSlug(entrada.slug());
        if (existente.isPresent()) {
            return reenvioDoMesmoOrganizador(existente.get(), contaOrganizadora);
        }

        Evento submetido = entrada.submetidoPor(contaOrganizadora, relogio.instant().truncatedTo(ChronoUnit.MICROS));
        try {
            return repository.salvar(submetido);
        } catch (EventoJaExistenteException gravadoAoMesmoTempo) {
            return repository.buscarPorSlug(entrada.slug())
                    .map(gravado -> reenvioDoMesmoOrganizador(gravado, contaOrganizadora))
                    .orElseThrow(() -> gravadoAoMesmoTempo);
        }
    }

    private static Evento reenvioDoMesmoOrganizador(Evento existente, UUID contaOrganizadora) {
        if (existente.foiSubmetidoPor(contaOrganizadora)) {
            return existente;
        }
        throw new EventoJaExistenteException();
    }
}
