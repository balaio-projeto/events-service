package br.com.balaio.events.event.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import br.com.balaio.events.event.application.port.ContatoContaPort;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@SpringBootTest
@AutoConfigureMockMvc
class ModeracaoEventosHttpTests {
    private static final String ISSUER = "balaio-local";
    private static final String AUDIENCE = "balaio-api";
    private static final UUID ORGANIZADOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID ADMIN = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID COMPRADOR = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final KeyPair CHAVE = gerarPar();
    private static final String DETALHES = "\"cep\":\"50050000\",\"rua\":\"Rua da Aurora\",\"numero\":\"240\","
            + "\"bairro\":\"Boa Vista\",\"complemento\":\"Galpão 3\",\"referencia\":\"Ao lado da ponte\","
            + "\"site\":\"https://feira.example.com\",\"contatoNome\":\"Ana Ceramista\","
            + "\"contatoTelefone\":\"(81) 98888-7777\",\"contatoEmail\":\"ana@example.com\",";

    @MockitoBean
    private ContatoContaPort contas;

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) throws IOException {
        Path publica = Files.createTempFile("jwt-public-", ".pem");
        Files.writeString(publica, "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(CHAVE.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n", StandardCharsets.US_ASCII);
        publica.toFile().deleteOnExit();
        registry.add("balaio.jwt.public-key-location", () -> publica.toString());
        registry.add("balaio.jwt.issuer", () -> ISSUER);
        registry.add("balaio.jwt.audience", () -> AUDIENCE);
    }

    @Test
    void organizadorSubmeteAdminAprovaEOEventoEntraNaAgenda() throws Exception {
        String slug = novoSlug("aprovado");

        submeter(slug, "organizador", ORGANIZADOR)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.slug").value(slug))
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"))
                .andExpect(jsonPath("$.erro").doesNotExist());

        mvc.perform(get("/api/v1/eventos"))
                .andExpect(jsonPath("$.dados[*].slug", not(hasItem(slug))));
        mvc.perform(get("/api/v1/eventos/" + slug)).andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[*].evento.slug", hasItem(slug)))
                .andExpect(jsonPath("$.dados[*].status", everyItem(is("PENDENTE"))));

        mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.organizadorId").value(ORGANIZADOR.toString()))
                .andExpect(jsonPath("$.dados.evento.organizador").value("Associação dos Ceramistas"))
                .andExpect(jsonPath("$.dados.submetidoEm", notNullValue()))
                .andExpect(jsonPath("$.dados.publicado").value(false));

        String primeiraDecisao = decidir(slug, "{\"decisao\":\"aprovar\"}", ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("APROVADO"))
                .andExpect(jsonPath("$.dados.publicado").value(true))
                .andExpect(jsonPath("$.dados.decididoPor").value(ADMIN.toString()))
                .andExpect(jsonPath("$.dados.decididoEm", notNullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String decididoEm = JsonPath.read(primeiraDecisao, "$.dados.decididoEm");

        decidir(slug, "{\"decisao\":\"aprovar\"}", UUID.randomUUID())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("APROVADO"))
                .andExpect(jsonPath("$.dados.decididoPor").value(ADMIN.toString()))
                .andExpect(jsonPath("$.dados.decididoEm").value(decididoEm));

        decidir(slug, "{\"decisao\":\"rejeitar\",\"motivo\":\"Mudei de ideia\"}", ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro.codigo").value("EVENTO_JA_DECIDIDO"));

        mvc.perform(get("/api/v1/eventos"))
                .andExpect(jsonPath("$.dados[*].slug", hasItem(slug)));
        mvc.perform(get("/api/v1/eventos/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.slug").value(slug));
        mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(jsonPath("$.dados[*].evento.slug", not(hasItem(slug))));
    }

    @Test
    void rejeicaoExigeMotivoENuncaPublica() throws Exception {
        String slug = novoSlug("rejeitado");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());

        decidir(slug, "{\"decisao\":\"rejeitar\",\"motivo\":\"   \"}", ADMIN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.erro.campos.motivo", notNullValue()));

        decidir(slug, "{\"decisao\":\"rejeitar\",\"motivo\":\"Faltou confirmar o local.\"}", ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("REJEITADO"))
                .andExpect(jsonPath("$.dados.publicado").value(false))
                .andExpect(jsonPath("$.dados.motivo").value("Faltou confirmar o local."));

        decidir(slug, "{\"decisao\":\"rejeitar\",\"motivo\":\"De novo\"}", ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.motivo").value("Faltou confirmar o local."));
        decidir(slug, "{\"decisao\":\"aprovar\"}", ADMIN)
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/eventos")).andExpect(jsonPath("$.dados[*].slug", not(hasItem(slug))));
        mvc.perform(get("/api/v1/eventos/" + slug)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/admin/eventos").param("estado", "rejeitado")
                        .header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[*].evento.slug", hasItem(slug)));
    }

    @Test
    void somenteAdminConsultaEDecide() throws Exception {
        String slug = novoSlug("papeis");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/admin/eventos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro.codigo").value("NAO_AUTENTICADO"));
        mvc.perform(post("/api/v1/admin/eventos/" + slug + "/decisao")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decisao\":\"aprovar\"}"))
                .andExpect(status().isUnauthorized());
        for (String papel : new String[] { "comprador", "artesao", "organizador" }) {
            mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer(papel, COMPRADOR)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
            mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer(papel, COMPRADOR)))
                    .andExpect(status().isForbidden());
            decidirComo(slug, "{\"decisao\":\"aprovar\"}", papel, COMPRADOR)
                    .andExpect(status().isForbidden());
        }

        mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer("admin", ADMIN)))
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"));
    }

    @Test
    void submissaoExigeOrganizadorEIgnoraIdentidadeDoCorpo() throws Exception {
        String slug = novoSlug("identidade");

        submeter(slug, null, null).andExpect(status().isUnauthorized());
        submeter(slug, "comprador", COMPRADOR).andExpect(status().isForbidden());
        submeter(slug, "admin", ADMIN).andExpect(status().isForbidden());

        String corpo = corpoEvento(slug).replace("{\"slug\"", "{\"organizadorId\":\"" + COMPRADOR
                + "\",\"status\":\"APROVADO\",\"slug\"");
        mvc.perform(post("/api/v1/eventos").header("Authorization", bearer("organizador", ORGANIZADOR))
                        .header("X-User-Id", COMPRADOR.toString())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"));

        mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer("admin", ADMIN)))
                .andExpect(jsonPath("$.dados.organizadorId").value(ORGANIZADOR.toString()))
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"));
    }

    @Test
    void submissaoInvalidaRetornaErrosPorCampoESlugRepetidoRetorna409() throws Exception {
        String invalido = "{\"slug\":\"x\",\"nome\":\"\",\"tipo\":\"feira\",\"organizador\":\" \",\"descricao\":\"d\","
                + "\"periodo\":\"p\",\"entrada\":\"Gratuita\",\"cidade\":\"Recife\",\"local\":\"l\",\"endereco\":\"e\","
                + "\"lat\":95,\"artesaos\":[],\"coletivos\":[]}";
        mvc.perform(post("/api/v1/eventos").header("Authorization", bearer("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.erro.campos.nome").value("Dê um nome ao evento."))
                .andExpect(jsonPath("$.erro.campos.organizador", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.lat", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.lng", notNullValue()));

        mvc.perform(post("/api/v1/eventos").header("Authorization", bearer("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.codigo").value("REQUISICAO_INVALIDA"));

        String slug = novoSlug("repetido");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());
        submeter(slug, "organizador", COMPRADOR)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro.codigo").value("EVENTO_JA_EXISTENTE"));
    }

    @Test
    void enderecoDetalhadoEContatoSaoObrigatoriosNaSubmissao() throws Exception {
        String semDetalhes = corpoEvento(novoSlug("sem-detalhes"))
                .replace(DETALHES, "\"cep\":\"\",\"rua\":\" \",");
        mvc.perform(post("/api/v1/eventos").header("Authorization", bearer("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(semDetalhes))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.erro.campos.rua").value("Informe a rua do evento."))
                .andExpect(jsonPath("$.erro.campos.numero", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.bairro", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.contatoNome", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.contatoTelefone", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.cep").doesNotExist())
                .andExpect(jsonPath("$.erro.campos.site").doesNotExist())
                .andExpect(jsonPath("$.erro.campos.contatoEmail").doesNotExist());

        String formatosInvalidos = corpoEvento(novoSlug("formatos"))
                .replace("\"cep\":\"50050000\"", "\"cep\":\"5005-000\"")
                .replace("\"site\":\"https://feira.example.com\"", "\"site\":\"ftp://feira.example.com\"")
                .replace("\"contatoTelefone\":\"(81) 98888-7777\"", "\"contatoTelefone\":\"98888-777\"")
                .replace("\"contatoEmail\":\"ana@example.com\"", "\"contatoEmail\":\"ana@\"");
        mvc.perform(post("/api/v1/eventos").header("Authorization", bearer("organizador", ORGANIZADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(formatosInvalidos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.campos.cep").value("Informe o CEP no formato 00000-000."))
                .andExpect(jsonPath("$.erro.campos.site", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.contatoTelefone", notNullValue()))
                .andExpect(jsonPath("$.erro.campos.contatoEmail", notNullValue()));
    }

    @Test
    void reenvioComOMesmoSlugPeloMesmoOrganizadorDevolveOEventoJaSubmetido() throws Exception {
        String slug = novoSlug("reenvio");

        String primeira = submeter(slug, "organizador", ORGANIZADOR)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String reenvio = submeter(slug, "organizador", ORGANIZADOR)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.slug").value(slug))
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"))
                .andExpect(jsonPath("$.erro").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(reenvio).isEqualTo(primeira);
        mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(jsonPath("$.dados[?(@.evento.slug == '" + slug + "')]", hasSize(1)));
    }

    @Test
    void moderacaoMostraContatoDoEventoEContaQueSolicitou() throws Exception {
        when(contas.consultar(ORGANIZADOR))
                .thenReturn(Optional.of(new ContatoContaPort.Contato("Ana Organizadora", "ana.conta@example.com")));
        String slug = novoSlug("solicitante");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.contato.nome").value("Ana Ceramista"))
                .andExpect(jsonPath("$.dados.contato.telefone").value("(81) 98888-7777"))
                .andExpect(jsonPath("$.dados.contato.email").value("ana@example.com"))
                .andExpect(jsonPath("$.dados.solicitante.id").value(ORGANIZADOR.toString()))
                .andExpect(jsonPath("$.dados.solicitante.nome").value("Ana Organizadora"))
                .andExpect(jsonPath("$.dados.solicitante.email").value("ana.conta@example.com"))
                .andExpect(jsonPath("$.dados.evento.rua").value("Rua da Aurora"))
                .andExpect(jsonPath("$.dados.evento.cep").value("50050-000"));

        mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[?(@.evento.slug == '" + slug + "')].solicitante.nome",
                        hasItem("Ana Organizadora")));

        decidir(slug, "{\"decisao\":\"aprovar\"}", ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.solicitante.email").value("ana.conta@example.com"))
                .andExpect(jsonPath("$.dados.contato.telefone").value("(81) 98888-7777"));
    }

    @Test
    void moderacaoContinuaFuncionandoQuandoOProfileFalha() throws Exception {
        when(contas.consultar(any())).thenThrow(new IllegalStateException("profile fora do ar"));
        String slug = novoSlug("profile-falhou");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/admin/eventos/" + slug).header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.solicitante.id").value(ORGANIZADOR.toString()))
                .andExpect(jsonPath("$.dados.solicitante.nome").value(nullValue()))
                .andExpect(jsonPath("$.dados.solicitante.email").value(nullValue()))
                .andExpect(jsonPath("$.dados.contato.nome").value("Ana Ceramista"));

        mvc.perform(get("/api/v1/admin/eventos").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[*].evento.slug", hasItem(slug)));
    }

    @Test
    void paginaPublicaMostraEnderecoDetalhadoMasNuncaOsDadosPrivados() throws Exception {
        String slug = novoSlug("publico");
        submeter(slug, "organizador", ORGANIZADOR).andExpect(status().isCreated());
        decidir(slug, "{\"decisao\":\"aprovar\"}", ADMIN).andExpect(status().isOk());

        String detalhe = mvc.perform(get("/api/v1/eventos/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.cep").value("50050-000"))
                .andExpect(jsonPath("$.dados.rua").value("Rua da Aurora"))
                .andExpect(jsonPath("$.dados.numero").value("240"))
                .andExpect(jsonPath("$.dados.bairro").value("Boa Vista"))
                .andExpect(jsonPath("$.dados.complemento").value("Galpão 3"))
                .andExpect(jsonPath("$.dados.referencia").value("Ao lado da ponte"))
                .andExpect(jsonPath("$.dados.site").value("https://feira.example.com"))
                .andExpect(jsonPath("$.dados.contatoNome").doesNotExist())
                .andExpect(jsonPath("$.dados.contatoTelefone").doesNotExist())
                .andExpect(jsonPath("$.dados.contatoEmail").doesNotExist())
                .andExpect(jsonPath("$.dados.contato").doesNotExist())
                .andExpect(jsonPath("$.dados.organizadorId").doesNotExist())
                .andExpect(jsonPath("$.dados.solicitante").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String agenda = mvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[?(@.slug == '" + slug + "')].rua", hasItem("Rua da Aurora")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        for (String publico : new String[] { detalhe, agenda }) {
            assertThat(publico).doesNotContain("contatoNome", "contatoTelefone", "contatoEmail", "organizadorId",
                    "Ana Ceramista", "98888-7777", "ana@example.com", ORGANIZADOR.toString());
        }
    }

    @Test
    void decisaoEFiltroInvalidosOuEventoInexistente() throws Exception {
        decidir("nao-existe-" + UUID.randomUUID(), "{\"decisao\":\"aprovar\"}", ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro.codigo").value("RECURSO_NAO_ENCONTRADO"));
        mvc.perform(get("/api/v1/admin/eventos/nao-existe").header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isNotFound());
        decidir("qualquer", "{\"decisao\":\"publicar\"}", ADMIN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.campos.decisao", notNullValue()));
        decidir("qualquer", "{}", ADMIN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.campos.decisao", notNullValue()));
        mvc.perform(get("/api/v1/admin/eventos").param("estado", "PUBLICADO")
                        .header("Authorization", bearer("admin", ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro.campos.estado", notNullValue()));
    }

    @Test
    void agendaPublicaTrazOsEventosDeDemonstracaoOrdenadosPelaDistancia() throws Exception {
        mvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[*].slug", hasItem("feira-de-artesanato-do-recife-antigo")))
                .andExpect(jsonPath("$.dados[*].slug", hasItem("festival-de-arte-popular-do-alto-da-se")))
                .andExpect(jsonPath("$.dados[*].slug", hasItem("feira-de-caruaru-artesanato-do-barro")));

        mvc.perform(get("/api/v1/eventos").param("lat", "-8.2829").param("lng", "-35.9722"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].slug").value("feira-de-caruaru-artesanato-do-barro"));
        mvc.perform(get("/api/v1/eventos").param("lat", "-8.0089").param("lng", "-34.8553"))
                .andExpect(jsonPath("$.dados[0].slug").value("festival-de-arte-popular-do-alto-da-se"));
        mvc.perform(get("/api/v1/eventos").param("lat", "-91").param("lng", "-34.8"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions submeter(String slug, String papel, UUID conta) throws Exception {
        var requisicao = post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content(corpoEvento(slug));
        if (papel != null) {
            requisicao.header("Authorization", bearer(papel, conta));
        }
        return mvc.perform(requisicao);
    }

    private ResultActions decidir(String slug, String corpo, UUID admin) throws Exception {
        return decidirComo(slug, corpo, "admin", admin);
    }

    private ResultActions decidirComo(String slug, String corpo, String papel, UUID conta) throws Exception {
        return mvc.perform(post("/api/v1/admin/eventos/" + slug + "/decisao")
                .header("Authorization", bearer(papel, conta))
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String novoSlug(String prefixo) {
        return "teste-" + prefixo + "-" + UUID.randomUUID();
    }

    private static String corpoEvento(String slug) {
        return "{\"slug\":\"" + slug + "\",\"nome\":\"Feira de Teste\",\"tipo\":\"feira\","
                + "\"organizador\":\"Associação dos Ceramistas\",\"descricao\":\"Venda direta de cerâmica.\","
                + "\"periodo\":\"12 de setembro, 2026\",\"horario\":\"9h às 17h\",\"entrada\":\"Gratuita\","
                + "\"cidade\":\"Petrolina\",\"local\":\"Orla\",\"endereco\":\"Rua da Aurora, 240 - Boa Vista\","
                + DETALHES
                + "\"lat\":-9.3891,\"lng\":-40.503,\"artesaos\":[],\"coletivos\":[]}";
    }

    private static String bearer(String papel, UUID conta) {
        try {
            Instant agora = Instant.now();
            var claims = new JWTClaimsSet.Builder()
                    .subject(conta.toString())
                    .claim("papel", papel)
                    .issuer(ISSUER)
                    .audience(AUDIENCE)
                    .issueTime(Date.from(agora))
                    .expirationTime(Date.from(agora.plusSeconds(3600)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();
            var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims);
            jwt.sign(new RSASSASigner(CHAVE.getPrivate()));
            return "Bearer " + jwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
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
}
