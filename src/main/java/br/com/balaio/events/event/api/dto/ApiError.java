package br.com.balaio.events.event.api.dto;

public record ApiError(
        String codigo,
        String mensagem
) {
}
