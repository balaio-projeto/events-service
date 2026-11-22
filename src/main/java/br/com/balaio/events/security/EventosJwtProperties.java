package br.com.balaio.events.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração de validação do JWT no events-service (PI-210).
 * Contém somente a chave PÚBLICA do emissor, nunca a privada.
 */
@ConfigurationProperties("balaio.jwt")
public record EventosJwtProperties(String publicKeyLocation, String issuer, String audience) {
    public EventosJwtProperties {
        if (blank(publicKeyLocation) || blank(issuer) || blank(audience)) {
            throw new IllegalStateException("Configuração JWT do events-service incompleta.");
        }
    }

    private static boolean blank(String valor) {
        return valor == null || valor.isBlank();
    }
}
