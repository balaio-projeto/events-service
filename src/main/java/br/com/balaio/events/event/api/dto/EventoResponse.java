package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.Evento;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoResponse(
        Long id,
        String slug,
        String nome,
        String tipo,
        String organizador,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String entrada,
        String cidade,
        String local,
        String endereco,

        @JsonProperty("lat")
        Double latitude,

        @JsonProperty("lng")
        Double longitude
){

    public static EventoResponse fromDomain(Evento evento){
        return new EventoResponse(
                evento.id(),
                evento.slug(),
                evento.nome(),
                evento.tipo(),
                evento.organizador(),
                evento.descricao(),
                evento.dataInicio(),
                evento.dataFim(),
                evento.horarioInicio(),
                evento.horarioFim(),
                evento.entrada(),
                evento.cidade(),
                evento.local(),
                evento.endereco(),
                evento.latitude(),
                evento.longitude()
        );
    }
}
