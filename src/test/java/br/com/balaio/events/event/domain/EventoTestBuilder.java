package br.com.balaio.events.event.domain;

public class EventoTestBuilder {
    private Long id = 111111111L;
    private String slug = "feira_de_artesanato";
    private String nome = "Feira_de_Artesanato";
    private String tipo = "Feira";
    private String organizador = "Organização Balaio";
    private String descricao = "Evento cultural de Pernambuco";
    private StatusAprovacao statusAprovacao = StatusAprovacao.PENDENTE;
    private StatusPublicacao statusPublicacao = StatusPublicacao.NAO_PUBLICADO;

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
                id,slug,nome,tipo,
                organizador,descricao,
                statusAprovacao,statusPublicacao
        );
    }
}
