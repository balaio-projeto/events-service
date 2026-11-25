package br.com.balaio.events.event.domain;

import br.com.balaio.events.event.application.port.ContatoContaPort;
import br.com.balaio.events.event.application.usecase.IdentificarSolicitantesUseCase;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdentificarSolicitantesUseCaseTest {

    private static final UUID MARIA = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID JOAO = UUID.fromString("99999999-8888-7777-6666-555555555555");

    private final ContatoContaPort contas = mock(ContatoContaPort.class);
    private final IdentificarSolicitantesUseCase useCase = new IdentificarSolicitantesUseCase(contas);

    @Test
    void identificaAContaQueSubmeteuOEvento() {
        when(contas.consultar(MARIA)).thenReturn(Optional.of(new ContatoContaPort.Contato("Maria", "maria@example.com")));

        Solicitante solicitante = useCase.identificar(submetidoPor(MARIA, "a"));

        assertThat(solicitante).isEqualTo(new Solicitante(MARIA, "Maria", "maria@example.com"));
    }

    @Test
    void consultaCadaContaUmaUnicaVezMesmoComVariosEventos() {
        when(contas.consultar(MARIA)).thenReturn(Optional.of(new ContatoContaPort.Contato("Maria", "maria@example.com")));
        when(contas.consultar(JOAO)).thenReturn(Optional.of(new ContatoContaPort.Contato("João", "joao@example.com")));

        Map<UUID, Solicitante> solicitantes = useCase.identificar(List.of(
                submetidoPor(MARIA, "a"), submetidoPor(JOAO, "b"), submetidoPor(MARIA, "c"), submetidoPor(MARIA, "d")));

        assertThat(solicitantes).hasSize(2);
        assertThat(solicitantes.get(JOAO).nome()).isEqualTo("João");
        verify(contas, times(1)).consultar(MARIA);
        verify(contas, times(1)).consultar(JOAO);
    }

    @Test
    void contaInexistenteDevolveSomenteOIdentificador() {
        when(contas.consultar(MARIA)).thenReturn(Optional.empty());

        assertThat(useCase.identificar(submetidoPor(MARIA, "a"))).isEqualTo(Solicitante.somenteIdentificado(MARIA));
    }

    @Test
    void falhaNoProfileNaoImpedeAModeracao() {
        when(contas.consultar(any())).thenThrow(new IllegalStateException("profile fora do ar"));

        Map<UUID, Solicitante> solicitantes = useCase.identificar(List.of(submetidoPor(MARIA, "a"), submetidoPor(JOAO, "b")));

        assertThat(solicitantes.get(MARIA)).isEqualTo(new Solicitante(MARIA, null, null));
        assertThat(solicitantes.get(JOAO)).isEqualTo(new Solicitante(JOAO, null, null));
    }

    @Test
    void eventoSemContaRegistradaNaoTemSolicitante() {
        Evento demonstracao = EventoTestBuilder.umEvento().build();

        assertThat(useCase.identificar(demonstracao)).isNull();
        verify(contas, never()).consultar(any());
    }

    private static Evento submetidoPor(UUID conta, String slug) {
        return EventoTestBuilder.umEvento().comSlug(slug).build().submetidoPor(conta, Instant.now());
    }
}
