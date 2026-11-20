package br.com.balaio.events.event.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoTestBuilder {
    private Long id = 111111111L;
    private String slug = "feira_de_artesanato";
    private String nome = "Feira_de_Artesanato";
    private String tipo = "Feira";
    private String organizador = "Organização Balaio";
    private String descricao = "Evento cultural de Pernambuco";
    private StatusAprovacao statusAprovacao = StatusAprovacao.PENDENTE;
    private StatusPublicacao statusPublicacao = StatusPublicacao.NAO_PUBLICADO;

    private LocalDate dataInicio = LocalDate.of(2026, 11, 20);
    private LocalDate dataFim = LocalDate.of(2026, 11, 22);

    private LocalTime horarioInicio = LocalTime.of(9, 0);
    private LocalTime horarioFim = LocalTime.of(18, 0);

    private String entrada = "Gratuita";
    private String cidade = "Olinda";
    private String local = "Centro Histórico";
    private String endereco = "Endereço de exemplo, Olinda - PE";

    private Double latitude = -8.0138;
    private Double longitude = -34.8553;

    private EventoTestBuilder(){
    }

    public static EventoTestBuilder umEvento() {
        return new EventoTestBuilder();
    }

    public EventoTestBuilder comStatusAprovacao(StatusAprovacao status){
        this.statusAprovacao = status;
        return this;
    }

    public EventoTestBuilder comStatusPublicacao(StatusPublicacao status){
        this.statusPublicacao = status;
        return this;
    }

    public Evento build(){
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
                    longitude
            );
    }
}