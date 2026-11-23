
package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.infrastructure.persistence.EventoJpaEntity;
import br.com.balaio.events.event.infrastructure.persistence.EventoJpaRepository;
import br.com.balaio.events.event.infrastructure.persistence.EventoPersistenceAdapter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventoPersistenceAdapterTest {

    private EventoJpaEntity criarEntidadeMock() {
        EventoJpaEntity entity = mock(EventoJpaEntity.class);

        when(entity.getId()).thenReturn(1L);
        when(entity.getSlug())
                .thenReturn("feira-artesanato-olinda");
        when(entity.getNome())
                .thenReturn("Feira de Artesanato de Olinda");
        when(entity.getTipo()).thenReturn("feira");
        when(entity.getOrganizador())
                .thenReturn("Organização Balaio");
        when(entity.getDescricao())
                .thenReturn("Evento cultural");

        when(entity.getStatusAprovacao())
                .thenReturn(StatusAprovacao.APROVADO);

        when(entity.getStatusPublicacao())
                .thenReturn(StatusPublicacao.PUBLICADO);

        when(entity.getDataInicio())
                .thenReturn(LocalDate.of(2026, 11, 20));

        when(entity.getDataFim())
                .thenReturn(LocalDate.of(2026, 11, 22));

        when(entity.getHorarioInicio())
                .thenReturn(LocalTime.of(9, 0));

        when(entity.getHorarioFim())
                .thenReturn(LocalTime.of(18, 0));

        when(entity.getEntrada()).thenReturn("Gratuita");
        when(entity.getCidade()).thenReturn("Olinda");
        when(entity.getLocal()).thenReturn("Centro Histórico");
        when(entity.getEndereco()).thenReturn("Olinda - PE");

        when(entity.getLatitude()).thenReturn(-8.0138);
        when(entity.getLongitude()).thenReturn(-34.8553);

        when(entity.getPeriodo())
                .thenReturn("20 a 22 de novembro, 2026");

        when(entity.getHorario()).thenReturn("9h às 18h");

        when(entity.getArtesaos())
                .thenReturn(List.of("maria-do-barro"));

        when(entity.getColetivos())
                .thenReturn(List.of("coletivo-alto-da-se"));

        return entity;
    }

    @Test
    void deveConsultarSomenteEventosAprovadosEPublicados() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        when(repository.findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(List.of());

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        List<Evento> resultado =
                adapter.buscarEventosPublicos();

        assertTrue(resultado.isEmpty());

        verify(repository)
                .findByStatusAprovacaoAndStatusPublicacao(
                        StatusAprovacao.APROVADO,
                        StatusPublicacao.PUBLICADO
                );
    }

    @Test
    void deveConverterEntidadeJpaParaEventoDoDominio() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        EventoJpaEntity entity = criarEntidadeMock();

        when(repository.findByStatusAprovacaoAndStatusPublicacao(
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(List.of(entity));

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        List<Evento> resultado =
                adapter.buscarEventosPublicos();

        assertEquals(1, resultado.size());

        Evento evento = resultado.getFirst();

        assertAll(
                () -> assertEquals(1L, evento.id()),
                () -> assertEquals(
                        "feira-artesanato-olinda",
                        evento.slug()
                ),
                () -> assertEquals(
                        "Feira de Artesanato de Olinda",
                        evento.nome()
                ),
                () -> assertEquals("feira", evento.tipo()),
                () -> assertEquals(
                        StatusAprovacao.APROVADO,
                        evento.statusAprovacao()
                ),
                () -> assertEquals(
                        StatusPublicacao.PUBLICADO,
                        evento.statusPublicacao()
                ),
                () -> assertEquals(-8.0138, evento.latitude()),
                () -> assertEquals(-34.8553, evento.longitude()),
                () -> assertEquals(
                        "20 a 22 de novembro, 2026",
                        evento.periodo()
                ),
                () -> assertEquals("9h às 18h", evento.horario()),
                () -> assertEquals(
                        List.of("maria-do-barro"),
                        evento.artesaos()
                ),
                () -> assertEquals(
                        List.of("coletivo-alto-da-se"),
                        evento.coletivos()
                )
        );
    }

    @Test
    void deveConsultarEventoPublicoPorSlug() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        EventoJpaEntity entity = criarEntidadeMock();

        when(repository.findBySlugAndStatusAprovacaoAndStatusPublicacao(
                "feira-artesanato-olinda",
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(Optional.of(entity));

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        Optional<Evento> resultado =
                adapter.buscarEventoPublicoPorSlug(
                        "feira-artesanato-olinda"
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                "feira-artesanato-olinda",
                resultado.orElseThrow().slug()
        );

        verify(repository)
                .findBySlugAndStatusAprovacaoAndStatusPublicacao(
                        "feira-artesanato-olinda",
                        StatusAprovacao.APROVADO,
                        StatusPublicacao.PUBLICADO
                );
    }

    @Test
    void deveRetornarVazioQuandoSlugNaoForPublico() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        when(repository.findBySlugAndStatusAprovacaoAndStatusPublicacao(
                "evento-pendente",
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        )).thenReturn(Optional.empty());

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        assertTrue(
                adapter.buscarEventoPublicoPorSlug(
                        "evento-pendente"
                ).isEmpty()
        );
    }

    @Test
    void deveVerificarSeSlugJaExiste() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        when(repository.existsBySlug("slug-existente"))
                .thenReturn(true);

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        assertTrue(adapter.existePorSlug("slug-existente"));

        verify(repository).existsBySlug("slug-existente");
    }

    @Test
    void deveSalvarEventoUtilizandoJpaRepository() {
        EventoJpaRepository repository =
                mock(EventoJpaRepository.class);

        Evento evento = EventoTestBuilder.umEvento()
                .comId(null)
                .build();

        when(repository.save(any(EventoJpaEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        EventoPersistenceAdapter adapter =
                new EventoPersistenceAdapter(repository);

        Evento salvo = adapter.salvar(evento);

        assertAll(
                () -> assertNull(salvo.id()),
                () -> assertEquals(evento.slug(), salvo.slug()),
                () -> assertEquals(
                        evento.periodo(),
                        salvo.periodo()
                ),
                () -> assertEquals(
                        evento.artesaos(),
                        salvo.artesaos()
                ),
                () -> assertEquals(
                        evento.coletivos(),
                        salvo.coletivos()
                )
        );

        verify(repository).save(any(EventoJpaEntity.class));
    }
}
