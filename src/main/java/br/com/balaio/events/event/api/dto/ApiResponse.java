package br.com.balaio.events.event.api.dto;

public record ApiResponse<T>(
        T dados,
        ApiError erro
) {
}
