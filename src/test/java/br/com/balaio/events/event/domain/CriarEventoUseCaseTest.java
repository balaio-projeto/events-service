
package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.CriarEventoUseCase;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CriarEventoUseCaseTest {

    private static final UUID ORGANIZADOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID OUTRA_CONTA = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final Instant AGORA = Instant.parse("2026-11-22T12:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Test
    void deveCriarEventoComoPendenteENaoPublicado() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comId(99L)
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .comArtesaos(List.of("maria-do-barro"))
                .comColetivos(List.of("coletivo-alto-da-se"))
                .build();

        when(repository.buscarPorSlug(entrada.slug()))
                .thenReturn(Optional.empty());

        when(repository.salvar(any(Evento.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CriarEventoUseCase useCase =
                new CriarEventoUseCase(repository, RELOGIO);

        Evento salvo = useCase.executar(entrada, ORGANIZADOR);

        assertAll(
                () -> assertNull(salvo.id()),
                () -> assertEquals(
                        StatusAprovacao.PENDENTE,
                        salvo.statusAprovacao()
                ),
                () -> assertEquals(
                        StatusPublicacao.NAO_PUBLICADO,
                        salvo.statusPublicacao()
                ),
                () -> assertFalse(salvo.isPublicamenteVisivel()),
                () -> assertEquals(ORGANIZADOR, salvo.organizadorId()),
                () -> assertEquals(AGORA, salvo.submetidoEm()),
                () -> assertNull(salvo.decididoPor()),
                () -> assertEquals(entrada.organizador(), salvo.organizador()),
                () -> assertEquals(
                        List.of("maria-do-barro"),
                        salvo.artesaos()
                ),
                () -> assertEquals(
                        List.of("coletivo-alto-da-se"),
                        salvo.coletivos()
                ),
                () -> assertEquals(
                        entrada.periodo(),
                        salvo.periodo()
                ),
                () -> assertEquals(
                        entrada.horario(),
                        salvo.horario()
                ),
                () -> assertEquals(
                        entrada.detalhes(),
                        salvo.detalhes()
                )
        );

        verify(repository).salvar(any(Evento.class));
    }

    @Test
    void deveRejeitarSlugJaUsadoPorOutraConta() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("slug-existente")
                .build();

        when(repository.buscarPorSlug("slug-existente"))
                .thenReturn(Optional.of(entrada.submetidoPor(OUTRA_CONTA, AGORA)));

        CriarEventoUseCase useCase =
                new CriarEventoUseCase(repository, RELOGIO);

        assertThrows(
                EventoJaExistenteException.class,
                () -> useCase.executar(entrada, ORGANIZADOR)
        );

        verify(repository).buscarPorSlug("slug-existente");
        verify(repository, never()).salvar(any(Evento.class));
    }

    @Test
    void reenvioDoMesmoOrganizadorDevolveOEventoJaSubmetidoSemCriarOutro() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("slug-reenviado")
                .build();
        Evento jaSubmetido = entrada.submetidoPor(ORGANIZADOR, AGORA.minusSeconds(30));

        when(repository.buscarPorSlug("slug-reenviado"))
                .thenReturn(Optional.of(jaSubmetido));

        Evento devolvido = new CriarEventoUseCase(repository, RELOGIO)
                .executar(entrada, ORGANIZADOR);

        assertSame(jaSubmetido, devolvido);
        verify(repository, never()).salvar(any(Evento.class));
    }

    @Test
    void reenviosSimultaneosDoMesmoOrganizadorDevolvemOEventoGravadoPrimeiro() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("slug-simultaneo")
                .build();
        Evento gravadoPeloOutroEnvio = entrada.submetidoPor(ORGANIZADOR, AGORA);

        when(repository.buscarPorSlug("slug-simultaneo"))
                .thenReturn(Optional.empty(), Optional.of(gravadoPeloOutroEnvio));
        when(repository.salvar(any(Evento.class)))
                .thenThrow(new EventoJaExistenteException());

        Evento devolvido = new CriarEventoUseCase(repository, RELOGIO)
                .executar(entrada, ORGANIZADOR);

        assertSame(gravadoPeloOutroEnvio, devolvido);
    }

    @Test
    void envioSimultaneoDeOutraContaComMesmoSlugContinuaRecusado() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("slug-disputado")
                .build();

        when(repository.buscarPorSlug("slug-disputado"))
                .thenReturn(Optional.empty(), Optional.of(entrada.submetidoPor(OUTRA_CONTA, AGORA)));
        when(repository.salvar(any(Evento.class)))
                .thenThrow(new EventoJaExistenteException());

        CriarEventoUseCase useCase = new CriarEventoUseCase(repository, RELOGIO);

        assertThrows(
                EventoJaExistenteException.class,
                () -> useCase.executar(entrada, ORGANIZADOR)
        );
    }

    @Test
    void devePreservarDadosDoEventoAoSalvar() {
        EventoRepositoryPort repository =
                mock(EventoRepositoryPort.class);

        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("festival-cultural")
                .comPeriodo("10 a 12 de dezembro, 2026")
                .comHorario("10h às 20h")
                .build();

        when(repository.salvar(any(Evento.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CriarEventoUseCase useCase =
                new CriarEventoUseCase(repository, RELOGIO);

        Evento salvo = useCase.executar(entrada, ORGANIZADOR);

        assertAll(
                () -> assertEquals(
                        "festival-cultural",
                        salvo.slug()
                ),
                () -> assertEquals(
                        entrada.nome(),
                        salvo.nome()
                ),
                () -> assertEquals(
                        "10 a 12 de dezembro, 2026",
                        salvo.periodo()
                ),
                () -> assertEquals(
                        "10h às 20h",
                        salvo.horario()
                ),
                () -> assertEquals(
                        entrada.latitude(),
                        salvo.latitude()
                ),
                () -> assertEquals(
                        entrada.longitude(),
                        salvo.longitude()
                )
        );
    }
}
