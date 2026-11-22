package br.com.balaio.events.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SegurancaEventosConfigTest {
    @Test
    void aceitaCaminhoSimplesEUriFileDoComposeDaPlataforma() {
        assertThat(SegurancaEventosConfig.arquivoDaChave("file:/run/keys-public/jwt-public.pem"))
                .isEqualTo(Path.of(URI.create("file:/run/keys-public/jwt-public.pem")));
        assertThat(SegurancaEventosConfig.arquivoDaChave("/run/keys-public/jwt-public.pem"))
                .isEqualTo(Path.of("/run/keys-public/jwt-public.pem"));
    }
}
