package br.com.balaio.events.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@WebMvcTest(controllers = SegurancaEventosHttpTests.ControladorDeTeste.class)
@Import({ SegurancaEventosConfig.class, SegurancaEventosHttpTests.ControladorDeTeste.class })
class SegurancaEventosHttpTests {
    private static final String ISSUER = "balaio-local";
    private static final String AUDIENCE = "balaio-api";
    private static final UUID ORGANIZADOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID OUTRA_CONTA = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final KeyPair CHAVE = gerarPar();
    private static final KeyPair CHAVE_FORJADA = gerarPar();

    @Autowired
    private MockMvc mvc;


    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) throws IOException {
        Path publica = Files.createTempFile("jwt-public-", ".pem");
        Files.writeString(publica, pem("PUBLIC KEY", CHAVE.getPublic().getEncoded()), StandardCharsets.US_ASCII);
        publica.toFile().deleteOnExit();
        registry.add("balaio.jwt.public-key-location", () -> publica.toString());
        registry.add("balaio.jwt.issuer", () -> ISSUER);
        registry.add("balaio.jwt.audience", () -> AUDIENCE);
    }

    @Test
    void leituraDeEventosSegueSemCredenciais() throws Exception {
        mvc.perform(get("/api/v1/eventos")).andExpect(status().isOk());
    }

    @Test
    void submissaoSemJwtRetorna401ComEnvelope() throws Exception {
        mvc.perform(post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro.codigo").value("NAO_AUTENTICADO"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "expirado", "assinaturaForjada", "issuerErrado", "audienciaErrada", "papelDesconhecido",
            "semJti", "vidaMaiorQueUmaHora" })
    void jwtInvalidoRetorna401MesmoEmRotaPublica(String defeito) throws Exception {
        String token = tokenComDefeito(defeito);
        mvc.perform(post("/api/v1/eventos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/eventos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = { "comprador", "artesao", "admin" })
    void submissaoExigePapelOrganizadorEVedaOsDemais(String papel) throws Exception {
        mvc.perform(post("/api/v1/eventos").header("Authorization", "Bearer " + token(papel, ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
    }

    @Test
    void organizadorSubmeteComSubDoTokenComoIdentidade() throws Exception {
        mvc.perform(post("/api/v1/eventos").header("Authorization", "Bearer " + token("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"organizadorId\":\"" + OUTRA_CONTA + "\",\"papel\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value(ORGANIZADOR.toString()));
    }

    @Test
    void headersDeIdentidadeForjadosNaoSubstituemPapelNemSub() throws Exception {
        mvc.perform(post("/api/v1/eventos").header("Authorization", "Bearer " + token("comprador", OUTRA_CONTA))
                        .header("X-Role", "organizador").header("X-Role-Admin", "admin")
                        .header("X-User-Id", ORGANIZADOR.toString())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/eventos").header("Authorization", "Bearer " + token("organizador", ORGANIZADOR))
                        .header("X-User-Id", OUTRA_CONTA.toString()).header("X-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value(ORGANIZADOR.toString()));
    }

    @Test
    void decisaoAdministrativaAceitaSomenteAdmin() throws Exception {
        String rota = "/api/v1/admin/eventos/" + UUID.randomUUID() + "/decisao";
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(rota).header("Authorization", "Bearer " + token("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(rota).header("Authorization", "Bearer " + token("comprador", OUTRA_CONTA))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(rota).header("Authorization", "Bearer " + token("admin", OUTRA_CONTA))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    private static String tokenComDefeito(String defeito) throws Exception {
        Instant agora = Instant.now();
        var builder = base(agora);
        PrivateKey chave = CHAVE.getPrivate();
        switch (defeito) {
            case "expirado" -> builder.expirationTime(Date.from(agora.minusSeconds(60)))
                    .issueTime(Date.from(agora.minusSeconds(3660)));
            case "assinaturaForjada" -> chave = CHAVE_FORJADA.getPrivate();
            case "issuerErrado" -> builder.issuer("outro-emissor");
            case "audienciaErrada" -> builder.audience("outra-api");
            case "papelDesconhecido" -> builder.claim("papel", "root");
            case "semJti" -> builder.jwtID(null);
            case "vidaMaiorQueUmaHora" -> builder.expirationTime(Date.from(agora.plusSeconds(7200)));
            default -> throw new IllegalArgumentException(defeito);
        }
        return assinar(builder.build(), chave);
    }

    private static String token(String papel, UUID sub) throws Exception {
        return assinar(base(Instant.now()).subject(sub.toString()).claim("papel", papel).build(), CHAVE.getPrivate());
    }

    private static JWTClaimsSet.Builder base(Instant agora) {
        return new JWTClaimsSet.Builder()
                .subject(ORGANIZADOR.toString())
                .claim("papel", "organizador")
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(agora.plusSeconds(3600)))
                .jwtID(UUID.randomUUID().toString());
    }

    private static String assinar(JWTClaimsSet claims, PrivateKey chave) throws Exception {
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims);
        jwt.sign(new RSASSASigner(chave));
        return jwt.serialize();
    }

    private static KeyPair gerarPar() {
        try {
            var gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(3072);
            return gerador.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String pem(String tipo, byte[] der) {
        return "-----BEGIN " + tipo + "-----\n"
                + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(der)
                + "\n-----END " + tipo + "-----\n";
    }

    @RestController
    static class ControladorDeTeste {
        @GetMapping("/api/v1/eventos")
        String listar() {
            return "[]";
        }

        @PostMapping("/api/v1/eventos")
        ChamadorAutenticado submeter(@AuthenticationPrincipal Jwt jwt) {
            return ChamadorAutenticado.de(jwt);
        }

        @PostMapping("/api/v1/admin/eventos/{id}/decisao")
        String decidir(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
            return "{\"decididoPor\":\"" + ChamadorAutenticado.de(jwt).contaId() + "\"}";
        }
    }
}
