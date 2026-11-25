
package br.com.balaio.events.event.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EventoTestBuilder {

    private Long id = 1L;
    private String slug = "feira-artesanato-olinda";
    private String nome = "Feira de Artesanato de Olinda";
    private String tipo = "feira";
    private String organizador = "Organização Balaio";
    private String descricao = "Evento cultural de Pernambuco";

    private StatusAprovacao statusAprovacao =
            StatusAprovacao.PENDENTE;

    private StatusPublicacao statusPublicacao =
            StatusPublicacao.NAO_PUBLICADO;

    private LocalDate dataInicio =
            LocalDate.of(2026, 11, 20);

    private LocalDate dataFim =
            LocalDate.of(2026, 11, 22);

    private LocalTime horarioInicio =
            LocalTime.of(9, 0);

    private LocalTime horarioFim =
            LocalTime.of(18, 0);

    private String entrada = "Gratuita";
    private String cidade = "Olinda";
    private String local = "Centro Histórico";
    private String endereco = "Olinda - PE";

    private Double latitude = -8.0138;
    private Double longitude = -34.8553;

    private String periodo = "20 a 22 de novembro, 2026";
    private String horario = "9h às 18h";

    private List<String> artesaos =
            List.of("artesao-exemplo");

    private List<String> coletivos =
            List.of("coletivo-exemplo");

    private DetalhesEvento detalhes = new DetalhesEvento(
            "53020-140",
            "Rua do Amparo",
            "120",
            "Amparo",
            "Casa 2",
            "Em frente à igreja",
            "https://feira.example.com",
            "Maria do Barro",
            "(81) 99999-0000",
            "maria@example.com"
    );

    private EventoTestBuilder() {
    }


    public static EventoTestBuilder umEvento() {
        return new EventoTestBuilder();
    }

    public EventoTestBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public EventoTestBuilder comSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public EventoTestBuilder comStatusAprovacao(
            StatusAprovacao status
    ) {
        this.statusAprovacao = status;
        return this;
    }

    public EventoTestBuilder comStatusPublicacao(
            StatusPublicacao status
    ) {
        this.statusPublicacao = status;
        return this;
    }

    public EventoTestBuilder comCoordenadas(
            double latitude,
            double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public EventoTestBuilder comDatas(
            LocalDate inicio,
            LocalDate fim
    ) {
        this.dataInicio = inicio;
        this.dataFim = fim;
        return this;
    }

    public EventoTestBuilder comPeriodo(String periodo) {
        this.periodo = periodo;
        return this;
    }

    public EventoTestBuilder comHorario(String horario) {
        this.horario = horario;
        return this;
    }

    public EventoTestBuilder comArtesaos(
            List<String> artesaos
    ) {
        this.artesaos = artesaos;
        return this;
    }

    public EventoTestBuilder comColetivos(
            List<String> coletivos
    ) {
        this.coletivos = coletivos;
        return this;
    }

    public EventoTestBuilder comDetalhes(DetalhesEvento detalhes) {
        this.detalhes = detalhes;
        return this;
    }

    public Evento build() {
        return new Evento(
                id,
                slug,
                nome,
                tipo,
                organizador,
                descricao,
                statusAprovacao,
                statusPublicacao,
                dataInicio,
                dataFim,
                horarioInicio,
                horarioFim,
                entrada,
                cidade,
                local,
                endereco,
                latitude,
                longitude,
                periodo,
                horario,
                artesaos,
                coletivos
        ).comDetalhes(detalhes);
    }
}
