
package br.com.balaio.events.event.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventoTest {

    @Test
    void deveRetornarFalseQuandoPendenteENaoPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.PENDENTE)
                .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                .build();

        assertFalse(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRetornarFalseQuandoPendenteEPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.PENDENTE)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        assertFalse(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRetornarFalseQuandoAprovadoENaoPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                .build();

        assertFalse(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRetornarFalseQuandoRejeitadoENaoPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.REJEITADO)
                .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                .build();

        assertFalse(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRetornarFalseQuandoRejeitadoEPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.REJEITADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        assertFalse(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRetornarTrueQuandoAprovadoEPublicado() {
        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        assertTrue(evento.isPublicamenteVisivel());
    }

    @Test
    void deveRejeitarDataFinalAnteriorAInicial() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EventoTestBuilder.umEvento()
                        .comDatas(
                                LocalDate.of(2026, 12, 20),
                                LocalDate.of(2026, 12, 10)
                        )
                        .build()
        );
    }

    @Test
    void deveRejeitarSomenteUmaDataInformada() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EventoTestBuilder.umEvento()
                        .comDatas(
                                LocalDate.of(2026, 12, 20),
                                null
                        )
                        .build()
        );
    }

    @Test
    void deveAceitarDatasEstruturadasAusentes() {
        Evento evento = EventoTestBuilder.umEvento()
                .comDatas(null, null)
                .build();

        assertNull(evento.dataInicio());
        assertNull(evento.dataFim());
        assertNotNull(evento.periodo());
    }

    @Test
    void deveRejeitarLatitudeForaDoIntervalo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EventoTestBuilder.umEvento()
                        .comCoordenadas(91, -34.8)
                        .build()
        );
    }

    @Test
    void deveRejeitarLongitudeForaDoIntervalo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EventoTestBuilder.umEvento()
                        .comCoordenadas(-8.0, -181)
                        .build()
        );
    }

    @Test
    void deveRejeitarNaNEInfinito() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> EventoTestBuilder.umEvento()
                                .comCoordenadas(Double.NaN, -34.8)
                                .build()
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> EventoTestBuilder.umEvento()
                                .comCoordenadas(
                                        -8.0,
                                        Double.POSITIVE_INFINITY
                                )
                                .build()
                )
        );
    }

    @Test
    void devePreservarReferenciasDosParticipantes() {
        Evento evento = EventoTestBuilder.umEvento()
                .comArtesaos(List.of("maria-do-barro"))
                .comColetivos(List.of("coletivo-alto-da-se"))
                .build();

        assertEquals(
                List.of("maria-do-barro"),
                evento.artesaos()
        );

        assertEquals(
                List.of("coletivo-alto-da-se"),
                evento.coletivos()
        );
    }

    @Test
    void deveSubstituirListasNulasPorListasVazias() {
        Evento evento = EventoTestBuilder.umEvento()
                .comArtesaos(null)
                .comColetivos(null)
                .build();

        assertEquals(List.of(), evento.artesaos());
        assertEquals(List.of(), evento.coletivos());
    }
}
