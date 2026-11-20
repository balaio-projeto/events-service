package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoJpaRepository
        extends JpaRepository<EventoJpaEntity,Long> {

    List<EventoJpaEntity> findByStatusAprovacaoAndStatusPublicacao(
            StatusAprovacao statusAprovacao,
            StatusPublicacao statusPublicacao
    );

    Optional<EventoJpaEntity> findBySlugAndStatusAprovacaoAndStatusPublicacao(
            String slug,
            StatusAprovacao statusAprovacao,
            StatusPublicacao statusPublicacao
    );

    boolean existsBySlug(String slug);
}
