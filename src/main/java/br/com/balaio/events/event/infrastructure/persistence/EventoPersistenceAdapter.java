package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.stereotype.Repository;

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
                entity.getColetivos()
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

        EventoJpaEntity salvo = repository.save(entity);

        return toDomain(salvo);
    }

    @Override
    public boolean existePorSlug(String slug) {
        return repository.existsBySlug(slug);
    }
}
