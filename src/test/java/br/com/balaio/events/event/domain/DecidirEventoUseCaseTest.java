package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.application.Exception.CampoInvalidoException;
import br.com.balaio.events.event.application.Exception.EventoJaDecididoException;
import br.com.balaio.events.event.application.Exception.EventoNaoEncontradoException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.DecidirEventoUseCase;
import br.com.balaio.events.event.application.usecase.ListarEventosEmModeracaoUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DecidirEventoUseCaseTest {

    private static final UUID ADMIN = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID OUTRO_ADMIN = UUID.fromString("ffffffff-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID ORGANIZADOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Instant AGORA = Instant.parse("2026-11-22T15:30:00Z");
    private static final String SLUG = "feira-artesanato-olinda";

    private EventoRepositoryPort repository;
    private DecidirEventoUseCase useCase;

    @BeforeEach
    void preparar() {
        repository = mock(EventoRepositoryPort.class);
        useCase = new DecidirEventoUseCase(repository, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    private static Evento pendente() {
        return EventoTestBuilder.umEvento().build()
                .submetidoPor(ORGANIZADOR, AGORA.minusSeconds(3600));
    }

    @Test
    void aprovarPublicaRegistrandoResponsavelEData() {
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(pendente()));
        when(repository.registrarDecisao(any())).thenReturn(true);

        Evento decidido = useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN);

        ArgumentCaptor<Evento> registrado = ArgumentCaptor.forClass(Evento.class);
        verify(repository).registrarDecisao(registrado.capture());
        assertAll(
                () -> assertEquals(StatusAprovacao.APROVADO, decidido.statusAprovacao()),
                () -> assertEquals(StatusPublicacao.PUBLICADO, decidido.statusPublicacao()),
                () -> assertTrue(decidido.isPublicamenteVisivel()),
                () -> assertEquals(ADMIN, decidido.decididoPor()),
                () -> assertEquals(AGORA, decidido.decididoEm()),
                () -> assertNull(decidido.motivoRejeicao()),
                () -> assertEquals(ORGANIZADOR, decidido.organizadorId()),
                () -> assertEquals(decidido, registrado.getValue())
        );
    }

    @Test
    void rejeitarGuardaMotivoENuncaPublica() {
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(pendente()));
        when(repository.registrarDecisao(any())).thenReturn(true);

        Evento decidido = useCase.executar(SLUG, DecisaoModeracao.REJEITAR, "  Faltam datas confirmadas.  ", ADMIN);

        assertAll(
                () -> assertEquals(StatusAprovacao.REJEITADO, decidido.statusAprovacao()),
                () -> assertEquals(StatusPublicacao.NAO_PUBLICADO, decidido.statusPublicacao()),
                () -> assertFalse(decidido.isPublicamenteVisivel()),
                () -> assertEquals("Faltam datas confirmadas.", decidido.motivoRejeicao()),
                () -> assertEquals(ADMIN, decidido.decididoPor()),
                () -> assertEquals(AGORA, decidido.decididoEm())
        );
    }

    @Test
    void rejeitarSemMotivoFalhaNoCampoMotivoSemConsultarOuGravar() {
        for (String motivo : new String[] { null, "", "   " }) {
            CampoInvalidoException erro = assertThrows(CampoInvalidoException.class,
                    () -> useCase.executar(SLUG, DecisaoModeracao.REJEITAR, motivo, ADMIN));
            assertEquals("motivo", erro.campo());
        }
        verify(repository, never()).buscarPorSlug(any());
        verify(repository, never()).registrarDecisao(any());
    }

    @Test
    void motivoAcimaDoLimiteEhRecusado() {
        String longo = "a".repeat(Evento.LIMITE_MOTIVO_REJEICAO + 1);
        CampoInvalidoException erro = assertThrows(CampoInvalidoException.class,
                () -> useCase.executar(SLUG, DecisaoModeracao.REJEITAR, longo, ADMIN));
        assertEquals("motivo", erro.campo());
    }

    @Test
    void decisaoAusenteEhRecusada() {
        CampoInvalidoException erro = assertThrows(CampoInvalidoException.class,
                () -> useCase.executar(SLUG, null, null, ADMIN));
        assertEquals("decisao", erro.campo());
    }

    @Test
    void eventoInexistenteRetornaNaoEncontrado() {
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.empty());

        assertThrows(EventoNaoEncontradoException.class,
                () -> useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN));
        verify(repository, never()).registrarDecisao(any());
    }

    @Test
    void repetirAMesmaDecisaoEhIdempotenteEPreservaQuemDecidiuPrimeiro() {
        Evento aprovado = pendente().aprovadoPor(OUTRO_ADMIN, AGORA.minusSeconds(60));
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(aprovado));

        Evento resultado = useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN);

        assertEquals(aprovado, resultado);
        assertEquals(OUTRO_ADMIN, resultado.decididoPor());
        verify(repository, never()).registrarDecisao(any());
    }

    @Test
    void repetirRejeicaoEhIdempotente() {
        Evento rejeitado = pendente().rejeitadoPor(OUTRO_ADMIN, AGORA.minusSeconds(60), "Sem local definido.");
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(rejeitado));

        Evento resultado = useCase.executar(SLUG, DecisaoModeracao.REJEITAR, "Outro motivo", ADMIN);

        assertEquals("Sem local definido.", resultado.motivoRejeicao());
        verify(repository, never()).registrarDecisao(any());
    }

    @Test
    void decisaoContrariaAUmaJaTomadaRetornaConflito() {
        Evento rejeitado = pendente().rejeitadoPor(OUTRO_ADMIN, AGORA.minusSeconds(60), "Sem local definido.");
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(rejeitado));

        assertThrows(EventoJaDecididoException.class,
                () -> useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN));
        verify(repository, never()).registrarDecisao(any());
    }

    @Test
    void corridaComDecisaoIgualDevolveOEstadoGravado() {
        Evento gravadoPorOutro = pendente().aprovadoPor(OUTRO_ADMIN, AGORA.minusSeconds(1));
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(pendente()), Optional.of(gravadoPorOutro));
        when(repository.registrarDecisao(any())).thenReturn(false);

        Evento resultado = useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN);

        assertEquals(gravadoPorOutro, resultado);
    }

    @Test
    void corridaComDecisaoDiferenteRetornaConflito() {
        Evento gravadoPorOutro = pendente().rejeitadoPor(OUTRO_ADMIN, AGORA.minusSeconds(1), "Duplicado.");
        when(repository.buscarPorSlug(SLUG)).thenReturn(Optional.of(pendente()), Optional.of(gravadoPorOutro));
        when(repository.registrarDecisao(any())).thenReturn(false);

        assertThrows(EventoJaDecididoException.class,
                () -> useCase.executar(SLUG, DecisaoModeracao.APROVAR, null, ADMIN));
    }

    @Test
    void filaPadraoListaPendentes() {
        ListarEventosEmModeracaoUseCase listar = new ListarEventosEmModeracaoUseCase(repository);
        when(repository.buscarPorStatusAprovacao(StatusAprovacao.PENDENTE)).thenReturn(List.of(pendente()));

        assertEquals(1, listar.executar(null).size());
        listar.executar(StatusAprovacao.REJEITADO);

        verify(repository).buscarPorStatusAprovacao(StatusAprovacao.PENDENTE);
        verify(repository).buscarPorStatusAprovacao(StatusAprovacao.REJEITADO);
    }

    @Test
    void dominioSoDecideEventoPendente() {
        Evento aprovado = pendente().aprovadoPor(ADMIN, AGORA);

        assertThrows(IllegalStateException.class, () -> aprovado.aprovadoPor(ADMIN, AGORA));
        assertThrows(IllegalStateException.class, () -> aprovado.rejeitadoPor(ADMIN, AGORA, "motivo"));
        assertThrows(IllegalArgumentException.class, () -> pendente().rejeitadoPor(ADMIN, AGORA, " "));
        assertThrows(IllegalArgumentException.class, () -> pendente().aprovadoPor(null, AGORA));
    }

    @Test
    void submissaoIgnoraEstadoEIdentidadeVindosDaEntrada() {
        Evento entrada = EventoTestBuilder.umEvento()
                .comId(42L)
                .comStatusAprovacao(StatusAprovacao.APROVADO)
                .comStatusPublicacao(StatusPublicacao.PUBLICADO)
                .build();

        Evento submetido = entrada.submetidoPor(ORGANIZADOR, AGORA);

        assertAll(
                () -> assertNull(submetido.id()),
                () -> assertEquals(StatusAprovacao.PENDENTE, submetido.statusAprovacao()),
                () -> assertEquals(StatusPublicacao.NAO_PUBLICADO, submetido.statusPublicacao()),
                () -> assertEquals(ORGANIZADOR, submetido.organizadorId()),
                () -> assertEquals(AGORA, submetido.submetidoEm()),
                () -> assertNull(submetido.decididoPor())
        );
    }
}
