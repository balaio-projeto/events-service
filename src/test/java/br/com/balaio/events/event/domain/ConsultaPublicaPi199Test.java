
package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.api.dto.EventoResponse;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.ConsultarEventoPorSlugUseCase;
import br.com.balaio.events.event.domain.service.CalculadoraDistancia;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaPublicaPi199Test {

    private Evento eventoPublico() {
        return EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();
    }

    @Test
    void deveRetornarEventoPublicoPorSlug() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento evento = eventoPublico();

        when(repository.buscarEventoPublicoPorSlug(
                "feira-artesanato-olinda"
        )).thenReturn(Optional.of(evento));

        ConsultarEventoPorSlugUseCase useCase =
                new ConsultarEventoPorSlugUseCase(repository);

        Optional<Evento> resultado =
                useCase.executar("feira-artesanato-olinda");

        assertTrue(resultado.isPresent());
        assertEquals(evento, resultado.orElseThrow());

        verify(repository)
                .buscarEventoPublicoPorSlug(
                        "feira-artesanato-olinda"
                );
    }

    @Test
    void deveRetornarVazioQuandoSlugNaoExiste() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        when(repository.buscarEventoPublicoPorSlug(
                "evento-inexistente"
        )).thenReturn(Optional.empty());

        ConsultarEventoPorSlugUseCase useCase =
                new ConsultarEventoPorSlugUseCase(repository);

        assertTrue(
                useCase.executar("evento-inexistente").isEmpty()
        );
    }

    @Test
    void deveCalcularDistanciaZeroParaCoordenadasIguais() {
        double distancia = CalculadoraDistancia.calcular(
                -8.0476,
                -34.8770,
                -8.0476,
                -34.8770
        );

        assertEquals(0.0, distancia, 0.000001);
    }

    @Test
    void deveCalcularDistanciaPositivaEntreCidades() {
        double distancia = CalculadoraDistancia.calcular(
                -8.0476,
                -34.8770,
                -8.0138,
                -34.8553
        );

        assertTrue(distancia > 0);
        assertTrue(Double.isFinite(distancia));
    }

    @Test
    void deveCalcularDistanciaSimetrica() {
        double ida = CalculadoraDistancia.calcular(
                -8.0476,
                -34.8770,
                -8.0138,
                -34.8553
        );

        double volta = CalculadoraDistancia.calcular(
                -8.0138,
                -34.8553,
                -8.0476,
                -34.8770
        );

        assertEquals(ida, volta, 0.000001);
    }

    @Test
    void deveRetornarCamposEsperadosPeloFrontend() {
        Evento evento = eventoPublico();

        EventoResponse response =
                EventoResponse.fromDomain(evento);

        assertAll(
                () -> assertEquals(evento.slug(), response.slug()),
                () -> assertEquals(evento.nome(), response.nome()),
                () -> assertEquals("feira", response.tipo()),
                () -> assertEquals(
                        evento.organizador(),
                        response.organizador()
                ),
                () -> assertEquals(
                        evento.descricao(),
                        response.descricao()
                ),
                () -> assertEquals(
                        evento.periodo(),
                        response.periodo()
                ),
                () -> assertEquals(
                        evento.horario(),
                        response.horario()
                ),
                () -> assertEquals(
                        evento.latitude(),
                        response.lat()
                ),
                () -> assertEquals(
                        evento.longitude(),
                        response.lng()
                ),
                () -> assertEquals(
                        evento.artesaos(),
                        response.artesaos()
                ),
                () -> assertEquals(
                        evento.coletivos(),
                        response.coletivos()
                )
        );
    }

    @Test
    void deveRetornarListasVaziasQuandoNaoExistiremParticipantes() {
        Evento evento = EventoTestBuilder.umEvento()
                .comArtesaos(List.of())
                .comColetivos(List.of())
                .build();

        EventoResponse response =
                EventoResponse.fromDomain(evento);

        assertEquals(List.of(), response.artesaos());
        assertEquals(List.of(), response.coletivos());
    }
}
