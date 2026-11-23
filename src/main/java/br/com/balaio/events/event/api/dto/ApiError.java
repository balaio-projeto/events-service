package br.com.balaio.events.event.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record ApiError(
        String codigo,
        String mensagem,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> campos
) {

    public ApiError(String codigo, String mensagem) {
        this(codigo, mensagem, null);
    }
}
