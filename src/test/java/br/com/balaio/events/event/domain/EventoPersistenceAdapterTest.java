package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.infrastructure.persistence.EventoJpaEntity;
import br.com.balaio.events.event.infrastructure.persistence.EventoJpaRepository;
import br.com.balaio.events.event.infrastructure.persistence.EventoPersistenceAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class EventoPersistenceAdapterTest {

    @Test
    void deveConsultarSomenteEventosAprovadosEPublicados() {

        // Arrange
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        when(repository.findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(List.of());

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        // Act
        List<Evento> resultado =
                adapter.buscarEventosPublicos();

        // Assert
        assertTrue(resultado.isEmpty());

        verify(repository).findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        );
    }

    @Test
    void deveConverterEntidadeJpaParaEventoDoDominio() {
        EventoJpaRepository repository = mock(EventoJpaRepository.class);
        EventoJpaEntity entity = mock(EventoJpaEntity.class);

        when(entity.getId()).thenReturn(1L);
        when(entity.getSlug()).thenReturn("feira-artesanato");
        when(entity.getNome()).thenReturn("Feira de Artesanato");
        when(entity.getTipo()).thenReturn("FEIRA");
        when(entity.getOrganizador()).thenReturn("Organização Balaio");
        when(entity.getDescricao()).thenReturn("Evento cultural");
        when(entity.getStatusAprovacao())
                .thenReturn(StatusAprovacao.APROVADO);
        when(entity.getStatusPublicacao())
                .thenReturn(StatusPublicacao.PUBLICADO);

        when(repository.findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(List.of(entity));

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        List<Evento> resultado = adapter.buscarEventosPublicos();

        assertEquals(1, resultado.size());

        Evento evento = resultado.getFirst();

        assertAll(
                () -> assertEquals(1L, evento.id()),
                () -> assertEquals("feira-artesanato", evento.slug()),
                () -> assertEquals("Feira de Artesanato", evento.nome()),
                () -> assertEquals("FEIRA", evento.tipo()),
                () -> assertEquals("Organização Balaio", evento.organizador()),
                () -> assertEquals("Evento cultural", evento.descricao()),
                () -> assertEquals(StatusAprovacao.APROVADO,
                        evento.statusAprovacao()),
                () -> assertEquals(StatusPublicacao.PUBLICADO,
                        evento.statusPublicacao())
        );

        verify(repository).findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        );
    }
}