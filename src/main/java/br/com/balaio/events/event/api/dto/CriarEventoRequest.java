
package br.com.balaio.events.event.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CriarEventoRequest(

        @NotBlank
        @Size(max = 255)
        String slug,

        @NotBlank
        @Size(max = 255)
        String nome,

        @NotBlank
        @Pattern(
                regexp = "feira|festival|exposicao|oficina"
        )
        String tipo,

        @NotBlank
        @Size(max = 255)
        String organizador,

        @NotBlank
        String descricao,

        @NotBlank
        String periodo,

        String horario,

        @NotBlank
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
        Double lat,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double lng,

        @NotNull
        List<@NotBlank String> artesaos,

        @NotNull
        List<@NotBlank String> coletivos,

        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,

        Boolean criadoPorVoce
) {
}
