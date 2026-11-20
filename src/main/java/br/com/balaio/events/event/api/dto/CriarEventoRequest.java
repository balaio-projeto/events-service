package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record CriarEventoRequest(
        @NotBlank
        @Size(max = 255)
        String slug,

        @NotBlank
        @Size(max = 255)
        String nome,

        @NotBlank
        @Size(max = 130)
        String tipo,

        @NotBlank
        @Size(max = 255)
        String organizador,


        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,

        @Size(max = 255)
        String entrada,

        @NotBlank
        @Size(max = 150)
        String cidade,

        @NotBlank
        @Size(max = 255)
        String local,

        @NotBlank
        @Size(max = 500)
        String endereco,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude
) {
}
