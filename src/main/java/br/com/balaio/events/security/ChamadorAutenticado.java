package br.com.balaio.events.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Identidade do chamador derivada somente do JWT validado localmente.
 * Headers de identidade e campos do corpo nunca entram aqui. O UUID é o único dado de conta guardado pelo events-service.
 */
public record ChamadorAutenticado(UUID contaId, String papel) {
    public static ChamadorAutenticado de(Jwt jwt) {
        return new ChamadorAutenticado(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("papel"));
    }
}
