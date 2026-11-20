package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CriarEventoUseCase {

    private EventoRepositoryPort repository;

    public CriarEventoUseCase(EventoRepositoryPort repository){
        this.repository = repository;
    }

    public Evento executar(Evento evento) {

        if (repository.existePorSlug(evento.slug())) {
            throw new EventoJaExistenteException();
        }

        Evento novoEvento = new Evento(
                null,
                evento.slug(),
                evento.nome(),
                evento.tipo(),
                evento.organizador(),
                evento.descricao(),
                StatusAprovacao.PENDENTE,
                StatusPublicacao.NAO_PUBLICADO,
                evento.dataInicio(),
                evento.dataFim(),
                evento.horarioInicio(),
                evento.horarioFim(),
                evento.entrada(),
                evento.cidade(),
                evento.local(),
                evento.endereco(),
                evento.latitude(),
                evento.longitude()
        );

        return repository.salvar(novoEvento);
    }
}
