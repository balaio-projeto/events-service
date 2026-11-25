package br.com.balaio.events.event.infrastructure.integration;

import br.com.balaio.events.event.application.port.ContatoContaPort;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ContatoContaHttpAdapterTest {

    private static final String TOKEN = "balaio-local-internal-token-1234567890";
    private static final UUID CONTA = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private HttpServer profile;
    private final List<String> tokensRecebidos = new CopyOnWriteArrayList<>();
    private int statusResposta = 200;
    private String corpoResposta;

    @BeforeEach
    void subirProfileFalso() throws IOException {
        profile = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        profile.createContext("/internal/v1/contas/" + CONTA + "/contato", troca -> {
            tokensRecebidos.add(troca.getRequestHeaders().getFirst("X-Balaio-Service-Token"));
            byte[] corpo = corpoResposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(statusResposta, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        profile.start();
    }

    @AfterEach
    void derrubarProfileFalso() {
        profile.stop(0);
    }

    @Test
    void consultaOContatoDaContaComOTokenInterno() {
        corpoResposta = "{\"dados\":{\"id\":\"" + CONTA + "\",\"nome\":\"Maria do Barro\",\"email\":\"maria@example.com\"},\"erro\":null}";

        assertThat(adapter(TOKEN).consultar(CONTA))
                .contains(new ContatoContaPort.Contato("Maria do Barro", "maria@example.com"));
        assertThat(tokensRecebidos).containsExactly(TOKEN);
    }

    @Test
    void contaInexistenteNoProfileDevolveVazio() {
        statusResposta = 404;
        corpoResposta = "{\"dados\":null,\"erro\":{\"codigo\":\"RECURSO_NAO_ENCONTRADO\",\"mensagem\":\"Conta não encontrada.\"}}";

        assertThat(adapter(TOKEN).consultar(CONTA)).isEmpty();
    }

    @Test
    void falhaDoProfileDevolveVazio() {
        statusResposta = 500;
        corpoResposta = "{}";

        assertThat(adapter(TOKEN).consultar(CONTA)).isEmpty();
    }

    @Test
    void profileForaDoArDevolveVazio() {
        ContatoContaHttpAdapter adapter = adapter(TOKEN);
        profile.stop(0);

        assertThat(adapter.consultar(CONTA)).isEmpty();
    }

    @Test
    void tokenCurtoNemChegaAConsultarOProfile() {
        corpoResposta = "{\"dados\":{\"nome\":\"Maria\",\"email\":\"maria@example.com\"},\"erro\":null}";

        assertThat(adapter("curto").consultar(CONTA)).isEmpty();
        assertThat(adapter("").consultar(CONTA)).isEmpty();
        assertThat(tokensRecebidos).isEmpty();
    }

    private ContatoContaHttpAdapter adapter(String token) {
        return new ContatoContaHttpAdapter("http://127.0.0.1:" + profile.getAddress().getPort(), token);
    }
}
