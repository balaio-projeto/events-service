package br.com.balaio.events.event.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record Evento(
        Long id,
        String slug,
        String nome,
        String tipo,
        String organizador,
        String descricao,
        StatusAprovacao statusAprovacao,
        StatusPublicacao statusPublicacao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String entrada,
        String cidade,
        String local,
        String endereco,
        Double latitude,
        Double longitude
) {
    public boolean isPublicamenteVisivel(){
        return (statusAprovacao == StatusAprovacao.APROVADO
                && statusPublicacao == StatusPublicacao.PUBLICADO);
    }
}
