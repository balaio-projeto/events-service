package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    List<EventoJpaEntity> findByStatusAprovacaoOrderBySubmetidoEmAscIdAsc(StatusAprovacao statusAprovacao);

    Optional<EventoJpaEntity> findBySlug(String slug);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update EventoJpaEntity e
               set e.statusAprovacao = :aprovacao,
                   e.statusPublicacao = :publicacao,
                   e.decididoPor = :decididoPor,
                   e.decididoEm = :decididoEm,
                   e.motivoRejeicao = :motivoRejeicao
             where e.slug = :slug
               and e.statusAprovacao = br.com.balaio.events.event.domain.StatusAprovacao.PENDENTE
            """)
    int registrarDecisaoSePendente(
            @Param("slug") String slug,
            @Param("aprovacao") StatusAprovacao aprovacao,
            @Param("publicacao") StatusPublicacao publicacao,
            @Param("decididoPor") UUID decididoPor,
            @Param("decididoEm") Instant decididoEm,
            @Param("motivoRejeicao") String motivoRejeicao
    );
}
