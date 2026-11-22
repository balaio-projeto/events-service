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

    public Evento executar(Evento entrada) {

        if (repository.existePorSlug(entrada.slug())) {
            throw new EventoJaExistenteException();
        }

        Evento novoEvento = new Evento(
                null,
                entrada.slug(),
                entrada.nome(),
                entrada.tipo(),
                entrada.organizador(),
                entrada.descricao(),
                StatusAprovacao.PENDENTE,
                StatusPublicacao.NAO_PUBLICADO,
                entrada.dataInicio(),
                entrada.dataFim(),
                entrada.horarioInicio(),
                entrada.horarioFim(),
                entrada.entrada(),
                entrada.cidade(),
                entrada.local(),
                entrada.endereco(),
                entrada.latitude(),
                entrada.longitude(),
                entrada.periodo(),
                entrada.horario(),
                entrada.artesaos(),
                entrada.coletivos()
        );

        return repository.salvar(novoEvento);
    }
}
