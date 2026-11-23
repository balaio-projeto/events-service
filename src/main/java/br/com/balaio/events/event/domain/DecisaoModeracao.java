package br.com.balaio.events.event.domain;

import java.util.Locale;
import java.util.Optional;

public enum DecisaoModeracao {
    APROVAR(StatusAprovacao.APROVADO),
    REJEITAR(StatusAprovacao.REJEITADO);

    private final StatusAprovacao resultado;

    DecisaoModeracao(StatusAprovacao resultado) {
        this.resultado = resultado;
    }

    public StatusAprovacao resultado() {
        return resultado;
    }

    public static Optional<DecisaoModeracao> de(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        return switch (valor.strip().toLowerCase(Locale.ROOT)) {
            case "aprovar" -> Optional.of(APROVAR);
            case "rejeitar" -> Optional.of(REJEITAR);
            default -> Optional.empty();
        };
    }
}
