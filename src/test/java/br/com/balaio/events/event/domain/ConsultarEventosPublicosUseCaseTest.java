
package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.ConsultarEventosPublicosUseCase;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultarEventosPublicosUseCaseTest {

    @Test
    void deveConsultarEventosPublicos() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        when(repository.buscarEventosPublicos())
                .thenReturn(List.of(evento));

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado = useCase.executar();

        assertEquals(List.of(evento), resultado);

        verify(repository).buscarEventosPublicos();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremEventosPublicos() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        when(repository.buscarEventosPublicos())
                .thenReturn(List.of());

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado = useCase.executar();

        assertTrue(resultado.isEmpty());

        verify(repository).buscarEventosPublicos();
    }

    @Test
    void deveConsultarSemOrdenacaoQuandoCoordenadasAusentes() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento olinda = EventoTestBuilder.umEvento()
                .comSlug("olinda")
                .comCoordenadas(-8.0138, -34.8553)
                .build();

        Evento recife = EventoTestBuilder.umEvento()
                .comSlug("recife")
                .comCoordenadas(-8.0476, -34.8770)
                .build();

        when(repository.buscarEventosPublicos())
                .thenReturn(List.of(olinda, recife));

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado =
                useCase.executar(null, null);

        assertEquals(List.of(olinda, recife), resultado);
        verify(repository).buscarEventosPublicos();
    }

    @Test
    void deveOrdenarEventosPorDistanciaCrescente() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento olinda = EventoTestBuilder.umEvento()
                .comSlug("evento-olinda")
                .comCoordenadas(-8.0138, -34.8553)
                .build();

        Evento recife = EventoTestBuilder.umEvento()
                .comSlug("evento-recife")
                .comCoordenadas(-8.0476, -34.8770)
                .build();

        when(repository.buscarEventosPublicos())
                .thenReturn(List.of(olinda, recife));

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado =
                useCase.executar(-8.0476, -34.8770);

        assertEquals(2, resultado.size());
        assertSame(recife, resultado.get(0));
        assertSame(olinda, resultado.get(1));

        verify(repository).buscarEventosPublicos();
    }

    @Test
    void deveRejeitarLatitudeSemLongitude() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.executar(-8.0, null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarLongitudeSemLatitude() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.executar(null, -34.8)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCoordenadasForaDosLimites() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.executar(100.0, -34.8)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.executar(-8.0, -200.0)
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarNaNEValoresInfinitos() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.executar(Double.NaN, -34.8)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> useCase.executar(
                                -8.0,
                                Double.POSITIVE_INFINITY
                        )
                )
        );

        verifyNoInteractions(repository);
    }
}
