package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.ConsultarEventosPublicosUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ConsultarEventosPublicosUseCaseTest {

    @Test
    public void TestarConsultaEventosPublicosUseCase() {

        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento evento = new Evento(
                1L,
                "feira-de-artesanato",
                "Feira de Artesanato",
                "Feira",
                "Organização Balaio",
                "Evento cultural",
                StatusAprovacao.APROVADO,
                StatusPublicacao.PUBLICADO
        );

        List<Evento> eventosEsperados = List.of(evento);

        when(repository.buscarEventosPublicos())
                .thenReturn(List.of(evento));

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado = useCase.executar();

        // ASSERT
        assertEquals(eventosEsperados, resultado);

        verify(repository).buscarEventosPublicos();
    }

    @Test
    public void consultarListagemDeEventosVaziaUseCase(){
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);;

        List<Evento> eventosEsperados = List.of();

        when(repository.buscarEventosPublicos())
                .thenReturn(eventosEsperados);

        ConsultarEventosPublicosUseCase useCase =
                new ConsultarEventosPublicosUseCase(repository);

        List<Evento> resultado = useCase.executar();

        assertEquals(eventosEsperados, resultado);

        assertTrue(resultado.isEmpty());

        verify(repository).buscarEventosPublicos();
    }
}
