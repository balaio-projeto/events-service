package br.com.balaio.events.event.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DecisaoEventoRequest(
        @NotBlank(message = "Escolha aprovar ou rejeitar.")
        String decisao,

        @Size(max = 1000, message = "O motivo da rejeição deve ter até 1000 caracteres.")
        String motivo
) {
}
