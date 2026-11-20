package br.com.balaio.events.event.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class EventoTest {

    @Test
    void deveRetornarFalseQuandoForPendenteENaoPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.PENDENTE)
                .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertFalse(resultado);
    }

    @Test
    void deveRetornarFalseQuandoForPendenteEPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.PENDENTE)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertFalse(resultado);
    }//11

    @Test
    void deveRetornarFalseQuandoForAprovadoENaoPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                            .comStatusAprovacao(StatusAprovacao.APROVADO)
                            .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                            .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertFalse(resultado);
    }//11

    @Test
    void deveRetornarFalseQuandoForRejeitadoENaoPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.REJEITADO)
                .comStatusPublicacao(StatusPublicacao.NAO_PUBLICADO)
                .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertFalse(resultado);
    }

    @Test
    void deveRetornarFalseQuandoForRejeitadoEPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.REJEITADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertFalse(resultado);
    }

    @Test
    void deveRetornarTrueQuandoForAprovadoEPublicado(){

        Evento evento = EventoTestBuilder.umEvento()
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        boolean resultado = evento.isPublicamenteVisivel();

        assertTrue(resultado);
    }
}
