package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.domain.DetalhesEvento;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.EventoTestBuilder;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EventoPersistenceAdapter.class)
class EventoPersistenciaPostgresTest {

    private static final UUID ORGANIZADOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID ADMIN = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final List<String> SLUGS_DEMONSTRACAO = List.of(
            "feira-de-artesanato-do-recife-antigo",
            "festival-de-arte-popular-do-alto-da-se",
            "feira-de-caruaru-artesanato-do-barro");

    @Autowired
    private EventoPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbc;

    private Evento submeter(String prefixo) {
        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug(prefixo + "-" + UUID.randomUUID())
                .comCoordenadas(-9.3891, -40.503)
                .build();
        return adapter.salvar(entrada.submetidoPor(ORGANIZADOR, Instant.now().truncatedTo(ChronoUnit.MICROS)));
    }

    private static List<String> slugs(List<Evento> eventos) {
        return eventos.stream().map(Evento::slug).toList();
    }

    @Test
    void migracaoDeDemonstracaoPublicaTresEventosComCoordenadasReais() {
        List<Evento> publicos = adapter.buscarEventosPublicos();

        assertThat(slugs(publicos)).containsAll(SLUGS_DEMONSTRACAO);
        List<Evento> demonstracao = publicos.stream().filter(e -> SLUGS_DEMONSTRACAO.contains(e.slug())).toList();
        assertThat(demonstracao).extracting(Evento::cidade)
                .containsExactlyInAnyOrder("Recife", "Olinda", "Caruaru");
        assertThat(demonstracao).allSatisfy(evento -> {
            assertThat(evento.isPublicamenteVisivel()).isTrue();
            assertThat(evento.latitude()).isBetween(-8.4, -7.9);
            assertThat(evento.longitude()).isBetween(-36.1, -34.8);
        });
    }

    @Test
    void submissaoPersisteOrganizadorDoTokenEFicaForaDaAgenda() {
        Evento salvo = submeter("pendente");

        Evento lido = adapter.buscarPorSlug(salvo.slug()).orElseThrow();
        assertThat(lido.id()).isNotNull();
        assertThat(lido.statusAprovacao()).isEqualTo(StatusAprovacao.PENDENTE);
        assertThat(lido.organizadorId()).isEqualTo(ORGANIZADOR);
        assertThat(lido.submetidoEm()).isNotNull();
        assertThat(slugs(adapter.buscarPorStatusAprovacao(StatusAprovacao.PENDENTE))).contains(salvo.slug());
        assertThat(slugs(adapter.buscarEventosPublicos())).doesNotContain(salvo.slug());
        assertThat(adapter.buscarEventoPublicoPorSlug(salvo.slug())).isEmpty();
    }

    @Test
    void submissaoPersisteEnderecoDetalhadoEContatoDoOrganizador() {
        DetalhesEvento detalhes = new DetalhesEvento("50050000", "Rua da Aurora", "S/N", "Boa Vista",
                "Galpão 3", "Ao lado da ponte", "https://feira.example.com/inscricao",
                "Ana Ceramista", "(81) 98888-7777", "ana@example.com");
        Evento entrada = EventoTestBuilder.umEvento()
                .comSlug("detalhado-" + UUID.randomUUID())
                .comDetalhes(detalhes)
                .build();

        adapter.salvar(entrada.submetidoPor(ORGANIZADOR, Instant.now().truncatedTo(ChronoUnit.MICROS)));

        Evento lido = adapter.buscarPorSlug(entrada.slug()).orElseThrow();
        assertThat(lido.detalhes()).isEqualTo(detalhes);
        assertThat(lido.detalhes().cep()).isEqualTo("50050-000");
        assertThat(jdbc.queryForObject(
                "select contato_telefone from eventos where slug = ?", String.class, entrada.slug()))
                .isEqualTo("(81) 98888-7777");
    }

    @Test
    void eventosAnterioresAosDetalhesContinuamLegiveisSemEles() {
        List<Evento> demonstracao = adapter.buscarEventosPublicos().stream()
                .filter(e -> SLUGS_DEMONSTRACAO.contains(e.slug()))
                .toList();

        assertThat(demonstracao).isNotEmpty()
                .allSatisfy(evento -> assertThat(evento.detalhes()).isEqualTo(DetalhesEvento.VAZIO));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void gravacaoConcorrenteComSlugRepetidoViraEventoJaExistente() {
        Evento primeiro = submeter("concorrente");
        Evento copia = EventoTestBuilder.umEvento()
                .comSlug(primeiro.slug())
                .build()
                .submetidoPor(ORGANIZADOR, Instant.now().truncatedTo(ChronoUnit.MICROS));

        assertThatThrownBy(() -> adapter.salvar(copia))
                .isInstanceOf(EventoJaExistenteException.class);
    }

    @Test
    void aprovacaoRegistradaUmaUnicaVezTornaOEventoPublico() {
        Evento pendente = adapter.buscarPorSlug(submeter("aprovar").slug()).orElseThrow();
        Instant quando = Instant.now().truncatedTo(ChronoUnit.MICROS);

        assertThat(adapter.registrarDecisao(pendente.aprovadoPor(ADMIN, quando))).isTrue();
        assertThat(adapter.registrarDecisao(pendente.rejeitadoPor(ADMIN, quando, "tarde demais"))).isFalse();

        Evento aprovado = adapter.buscarPorSlug(pendente.slug()).orElseThrow();
        assertThat(aprovado.statusAprovacao()).isEqualTo(StatusAprovacao.APROVADO);
        assertThat(aprovado.statusPublicacao()).isEqualTo(StatusPublicacao.PUBLICADO);
        assertThat(aprovado.decididoPor()).isEqualTo(ADMIN);
        assertThat(aprovado.decididoEm()).isEqualTo(quando);
        assertThat(aprovado.motivoRejeicao()).isNull();
        assertThat(slugs(adapter.buscarEventosPublicos())).contains(pendente.slug());
        assertThat(slugs(adapter.buscarPorStatusAprovacao(StatusAprovacao.PENDENTE))).doesNotContain(pendente.slug());
    }

    @Test
    void rejeicaoGuardaMotivoENuncaApareceNaAgenda() {
        Evento pendente = adapter.buscarPorSlug(submeter("rejeitar").slug()).orElseThrow();

        assertThat(adapter.registrarDecisao(pendente.rejeitadoPor(ADMIN, Instant.now(), "Sem data confirmada."))).isTrue();

        Evento rejeitado = adapter.buscarPorSlug(pendente.slug()).orElseThrow();
        assertThat(rejeitado.statusAprovacao()).isEqualTo(StatusAprovacao.REJEITADO);
        assertThat(rejeitado.statusPublicacao()).isEqualTo(StatusPublicacao.NAO_PUBLICADO);
        assertThat(rejeitado.motivoRejeicao()).isEqualTo("Sem data confirmada.");
        assertThat(slugs(adapter.buscarPorStatusAprovacao(StatusAprovacao.REJEITADO))).contains(pendente.slug());
        assertThat(slugs(adapter.buscarEventosPublicos())).doesNotContain(pendente.slug());
        assertThat(adapter.buscarEventoPublicoPorSlug(pendente.slug())).isEmpty();
    }

    @Test
    void bancoRecusaRejeicaoSemMotivo() {
        Evento pendente = submeter("sem-motivo");

        assertThatThrownBy(() -> jdbc.update(
                "update eventos set status_aprovacao = 'REJEITADO', motivo_rejeicao = ' ' where slug = ?",
                pendente.slug()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoRecusaPublicacaoSemAprovacao() {
        Evento pendente = submeter("sem-aprovacao");

        assertThatThrownBy(() -> jdbc.update(
                "update eventos set status_publicacao = 'PUBLICADO' where slug = ?", pendente.slug()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filaListaPendentesDoMaisAntigoParaOMaisRecente() {
        Evento primeiro = submeter("fila-a");
        Evento segundo = submeter("fila-b");

        List<String> fila = slugs(adapter.buscarPorStatusAprovacao(StatusAprovacao.PENDENTE));

        assertThat(fila.indexOf(primeiro.slug())).isLessThan(fila.indexOf(segundo.slug()));
    }
}
