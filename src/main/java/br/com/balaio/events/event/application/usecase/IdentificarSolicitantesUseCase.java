package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.port.ContatoContaPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.Solicitante;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IdentificarSolicitantesUseCase {

    private final ContatoContaPort contas;

    public IdentificarSolicitantesUseCase(ContatoContaPort contas) {
        this.contas = contas;
    }

    public Map<UUID, Solicitante> identificar(Collection<Evento> eventos) {
        Map<UUID, Solicitante> solicitantesPorConta = new HashMap<>();
        for (Evento evento : eventos) {
            UUID conta = evento.organizadorId();
            if (conta != null) {
                solicitantesPorConta.computeIfAbsent(conta, this::consultar);
            }
        }
        return solicitantesPorConta;
    }

    public Solicitante identificar(Evento evento) {
        return identificar(List.of(evento)).get(evento.organizadorId());
    }

    private Solicitante consultar(UUID conta) {
        try {
            return contas.consultar(conta)
                    .map(contato -> new Solicitante(conta, contato.nome(), contato.email()))
                    .orElseGet(() -> Solicitante.somenteIdentificado(conta));
        } catch (RuntimeException profileIndisponivel) {
            return Solicitante.somenteIdentificado(conta);
        }
    }
}
