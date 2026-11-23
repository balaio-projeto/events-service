package br.com.balaio.events.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Reaplica no events-service as regras de claims do contrato do gateway e do profile:
 * sub UUID canônico, papel conhecido, jti presente, vida máxima de 3600 s e iat sem antecipação relevante.
 */
final class ValidadorClaimsEvento implements OAuth2TokenValidator<Jwt> {
    private static final Set<String> PAPEIS = Set.of("comprador", "artesao", "organizador", "admin");
    private static final long VIDA_MAXIMA_SEGUNDOS = 3600;
    private static final long TOLERANCIA_RELOGIO_SEGUNDOS = 30;
    private static final OAuth2TokenValidatorResult INVALIDO = OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Sessão inválida.", null));

    private final Clock relogio;

    ValidadorClaimsEvento(Clock relogio) {
        this.relogio = relogio;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (!uuidCanonico(jwt.getSubject()) || !PAPEIS.contains(jwt.getClaimAsString("papel"))
                || jwt.getId() == null || jwt.getId().isBlank()) {
            return INVALIDO;
        }
        Instant emitidoEm = jwt.getIssuedAt();
        Instant expiraEm = jwt.getExpiresAt();
        if (emitidoEm == null || expiraEm == null) {
            return INVALIDO;
        }
        Instant agora = relogio.instant();
        if (!expiraEm.isAfter(emitidoEm)
                || expiraEm.isAfter(emitidoEm.plusSeconds(VIDA_MAXIMA_SEGUNDOS))
                || emitidoEm.isAfter(agora.plusSeconds(TOLERANCIA_RELOGIO_SEGUNDOS))) {
            return INVALIDO;
        }
        return OAuth2TokenValidatorResult.success();
    }

    private static boolean uuidCanonico(String valor) {
        if (valor == null) {
            return false;
        }
        try {
            return UUID.fromString(valor).toString().equals(valor);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
