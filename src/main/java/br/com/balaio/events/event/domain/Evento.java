
package br.com.balaio.events.event.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
        Double longitude,
        String periodo,
        String horario,
        List<String> artesaos,
        List<String> coletivos
) {

    public Evento {
        artesaos = artesaos == null
                ? List.of()
                : List.copyOf(artesaos);

        coletivos = coletivos == null
                ? List.of()
                : List.copyOf(coletivos);

        if ((dataInicio == null) != (dataFim == null)) {
            throw new IllegalArgumentException(
                    "Datas inicial e final devem ser informadas juntas."
            );
        }

        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException(
                    "Data final anterior à data inicial."
            );
        }

        if (latitude == null || longitude == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Coordenadas geográficas inválidas."
            );
        }
    }

    public boolean isPublicamenteVisivel() {
        return statusAprovacao == StatusAprovacao.APROVADO
                && statusPublicacao == StatusPublicacao.PUBLICADO;
    }
}
