
package br.com.balaio.events.event.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CriarEventoRequest(

        @NotBlank(message = "Não foi possível gerar o endereço do evento.")
        @Size(max = 255, message = "O nome do evento ficou longo demais.")
        String slug,

        @NotBlank(message = "Dê um nome ao evento.")
        @Size(max = 255, message = "Use até 255 caracteres no nome.")
        String nome,

        @NotBlank(message = "Escolha o tipo de evento.")
        @Pattern(
                regexp = "feira|festival|exposicao|oficina",
                message = "Escolha feira, festival, exposição ou oficina."
        )
        String tipo,

        @NotBlank(message = "Diga quem organiza o evento.")
        @Size(max = 255, message = "Use até 255 caracteres para quem organiza.")
        String organizador,

        @NotBlank(message = "Conte o que vai acontecer no evento.")
        String descricao,

        @NotBlank(message = "Informe as datas do evento.")
        String periodo,

        String horario,

        @NotBlank(message = "Informe a entrada, como Gratuita.")
        String entrada,

        @NotBlank(message = "Escolha o município do evento.")
        @Size(max = 150, message = "Use até 150 caracteres no município.")
        String cidade,

        @NotBlank(message = "Informe o local do evento.")
        @Size(max = 255, message = "Use até 255 caracteres no local.")
        String local,

        @NotBlank(message = "Informe o endereço do evento.")
        @Size(max = 500, message = "Use até 500 caracteres no endereço.")
        String endereco,

        @NotNull(message = "Marque o ponto do evento no mapa.")
        @DecimalMin(value = "-90.0", message = "Marque um ponto válido no mapa.")
        @DecimalMax(value = "90.0", message = "Marque um ponto válido no mapa.")
        Double lat,

        @NotNull(message = "Marque o ponto do evento no mapa.")
        @DecimalMin(value = "-180.0", message = "Marque um ponto válido no mapa.")
        @DecimalMax(value = "180.0", message = "Marque um ponto válido no mapa.")
        Double lng,

        @NotNull(message = "Informe os artesãos participantes.")
        List<@NotBlank(message = "Participante inválido.") String> artesaos,

        @NotNull(message = "Informe os coletivos participantes.")
        List<@NotBlank(message = "Participante inválido.") String> coletivos,

        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,

        Boolean criadoPorVoce
) {
}
