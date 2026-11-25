package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EventoPersistenceAdapter implements EventoRepositoryPort {

    private final EventoJpaRepository repository;

    public EventoPersistenceAdapter(EventoJpaRepository repository){
        this.repository = repository;
    }

    private Evento toDomain(EventoJpaEntity entity) {
        return new Evento(
                entity.getId(),
                entity.getSlug(),
                entity.getNome(),
                entity.getTipo(),
                entity.getOrganizador(),
                entity.getDescricao(),
                entity.getStatusAprovacao(),
                entity.getStatusPublicacao(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getHorarioInicio(),
                entity.getHorarioFim(),
                entity.getEntrada(),
                entity.getCidade(),
                entity.getLocal(),
                entity.getEndereco(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getPeriodo(),
                entity.getHorario(),
                entity.getArtesaos(),
                entity.getColetivos(),
                entity.getOrganizadorId(),
                entity.getSubmetidoEm(),
                entity.getDecididoPor(),
                entity.getDecididoEm(),
                entity.getMotivoRejeicao(),
                entity.getDetalhes()
        );
    }

    @Override
    public List<Evento> buscarEventosPublicos(){
        List<EventoJpaEntity> entidades = repository.findByStatusAprovacaoAndStatusPublicacao
                (StatusAprovacao.APROVADO, StatusPublicacao.PUBLICADO);

        List<Evento> eventos = new ArrayList<>();

        for(EventoJpaEntity entity : entidades){
            eventos.add(toDomain(entity));
        }
        return eventos;
    }

    @Override
    public Optional<Evento> buscarEventoPublicoPorSlug(String slug) {
        return repository
                .findBySlugAndStatusAprovacaoAndStatusPublicacao(
                        slug,
                        StatusAprovacao.APROVADO,
                        StatusPublicacao.PUBLICADO
                )
                .map(this::toDomain);
    }

    @Override
    public Evento salvar(Evento evento){
        EventoJpaEntity entity = new EventoJpaEntity(evento);

        try {
            return toDomain(repository.save(entity));
        } catch (DataIntegrityViolationException violacao) {
            if (evento.id() == null && repository.existsBySlug(evento.slug())) {
                throw new EventoJaExistenteException();
            }
            throw violacao;
        }
    }

    @Override
    public boolean existePorSlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public List<Evento> buscarPorStatusAprovacao(StatusAprovacao statusAprovacao) {
        return repository.findByStatusAprovacaoOrderBySubmetidoEmAscIdAsc(statusAprovacao)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Evento> buscarPorSlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    @Transactional
    public boolean registrarDecisao(Evento eventoDecidido) {
        return repository.registrarDecisaoSePendente(
                eventoDecidido.slug(),
                eventoDecidido.statusAprovacao(),
                eventoDecidido.statusPublicacao(),
                eventoDecidido.decididoPor(),
                eventoDecidido.decididoEm(),
                eventoDecidido.motivoRejeicao()
        ) == 1;
    }
}
