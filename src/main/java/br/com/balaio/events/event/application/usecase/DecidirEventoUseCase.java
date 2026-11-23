package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.Exception.CampoInvalidoException;
import br.com.balaio.events.event.application.Exception.EventoJaDecididoException;
import br.com.balaio.events.event.application.Exception.EventoNaoEncontradoException;
import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.DecisaoModeracao;
import br.com.balaio.events.event.domain.Evento;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class DecidirEventoUseCase {

    private final EventoRepositoryPort repository;
    private final Clock relogio;

    public DecidirEventoUseCase(EventoRepositoryPort repository, Clock relogio) {
        this.repository = repository;
        this.relogio = relogio;
    }

    public Evento executar(String slug, DecisaoModeracao decisao, String motivo, UUID administrador) {
        if (decisao == null) {
            throw new CampoInvalidoException("decisao", "Escolha aprovar ou rejeitar.");
        }
        if (decisao == DecisaoModeracao.REJEITAR) {
            validarMotivo(motivo);
        }

        Evento atual = buscar(slug);
        if (!atual.isPendente()) {
            return confirmarMesmaDecisao(atual, decisao);
        }

        Instant agora = relogio.instant().truncatedTo(ChronoUnit.MICROS);
        Evento decidido = decisao == DecisaoModeracao.APROVAR
                ? atual.aprovadoPor(administrador, agora)
                : atual.rejeitadoPor(administrador, agora, motivo);

        if (repository.registrarDecisao(decidido)) {
            return decidido;
        }
        return confirmarMesmaDecisao(buscar(slug), decisao);
    }

    private Evento buscar(String slug) {
        return repository.buscarPorSlug(slug).orElseThrow(EventoNaoEncontradoException::new);
    }

    private static Evento confirmarMesmaDecisao(Evento evento, DecisaoModeracao decisao) {
        if (evento.statusAprovacao() == decisao.resultado()) {
            return evento;
        }
        throw new EventoJaDecididoException();
    }

    private static void validarMotivo(String motivo) {
        String limpo = motivo == null ? "" : motivo.strip();
        if (limpo.isEmpty()) {
            throw new CampoInvalidoException("motivo", "Explique ao organizador o motivo da rejeição.");
        }
        if (limpo.length() > Evento.LIMITE_MOTIVO_REJEICAO) {
            throw new CampoInvalidoException("motivo", "O motivo da rejeição deve ter até 1000 caracteres.");
        }
    }
}
