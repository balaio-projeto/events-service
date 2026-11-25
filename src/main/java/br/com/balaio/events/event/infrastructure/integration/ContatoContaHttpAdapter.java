package br.com.balaio.events.event.infrastructure.integration;

import br.com.balaio.events.event.application.port.ContatoContaPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContatoContaHttpAdapter implements ContatoContaPort {

    private static final int TAMANHO_MINIMO_TOKEN = 32;

    private final RestClient http;
    private final String token;

    public ContatoContaHttpAdapter(
            @Value("${balaio.profile-url}") String profileUrl,
            @Value("${balaio.internal-token:}") String token
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.http = RestClient.builder().baseUrl(profileUrl).requestFactory(factory).build();
        this.token = token == null ? "" : token;
    }

    @Override
    public Optional<Contato> consultar(UUID conta) {
        if (conta == null || token.length() < TAMANHO_MINIMO_TOKEN) {
            return Optional.empty();
        }
        try {
            RespostaContato resposta = http.get().uri("/internal/v1/contas/{id}/contato", conta)
                    .header("X-Balaio-Service-Token", token)
                    .retrieve()
                    .body(RespostaContato.class);
            return Optional.ofNullable(resposta)
                    .map(RespostaContato::dados)
                    .map(dados -> new Contato(dados.nome(), dados.email()));
        } catch (RestClientException profileIndisponivel) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaContato(DadosContato dados) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DadosContato(String nome, String email) {
    }
}
