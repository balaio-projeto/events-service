package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;

import java.util.List;
import java.util.Optional;

public class ListarEventosEmModeracaoUseCase {

    private final EventoRepositoryPort repository;

    public ListarEventosEmModeracaoUseCase(EventoRepositoryPort repository) {
        this.repository = repository;
    }

    public List<Evento> executar(StatusAprovacao estado) {
        return repository.buscarPorStatusAprovacao(estado == null ? StatusAprovacao.PENDENTE : estado);
    }

    public Optional<Evento> detalhar(String slug) {
        return repository.buscarPorSlug(slug);
    }
}
