package br.com.balaio.events.event.domain;

public record Evento(
        Long id,
        String slug,
        String nome,
        String tipo,
        String organizador,
        String descricao,
        StatusAprovacao statusAprovacao,
        StatusPublicacao statusPublicacao
) {
    public boolean isPublicamenteVisivel(){
        return (statusAprovacao == StatusAprovacao.APROVADO
                && statusPublicacao == StatusPublicacao.PUBLICADO);
    }
}
