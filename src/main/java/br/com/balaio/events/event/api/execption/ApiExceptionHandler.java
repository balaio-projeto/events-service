package br.com.balaio.events.event.api.execption;

import br.com.balaio.events.event.api.dto.ApiError;
import br.com.balaio.events.event.api.dto.ApiResponse;
import br.com.balaio.events.event.application.Exception.CampoInvalidoException;
import br.com.balaio.events.event.application.Exception.EventoJaDecididoException;
import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import br.com.balaio.events.event.application.Exception.EventoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> tratarRespostaHttp(
            ResponseStatusException exception
    ) {
        int status = exception.getStatusCode().value();

        String codigo = switch (status) {
            case 400 -> "REQUISICAO_INVALIDA";
            case 404 -> "RECURSO_NAO_ENCONTRADO";
            default -> "ERRO_INTERNO";
        };

        ApiError erro = new ApiError(
                codigo,
                exception.getReason() != null
                        ? exception.getReason()
                        : "Não foi possível processar a requisição."
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ApiResponse<>(null, erro));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> tratarParametroInvalido(
            MethodArgumentTypeMismatchException exception
    ) {
        ApiError erro = new ApiError(
                "REQUISICAO_INVALIDA",
                "O parâmetro '" + exception.getName()
                        + "' possui um valor inválido."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(null, erro));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> tratarValidacao(
            MethodArgumentNotValidException ex
    ) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(erro -> "Campo '" + erro.getField()
                        + "': " + erro.getDefaultMessage())
                .orElse("Dados inválidos.");

        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.putIfAbsent(campoRaiz(erro.getField()), erro.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        null,
                        new ApiError("REQUISICAO_INVALIDA", mensagem, campos.isEmpty() ? null : campos)
                ));
    }

    private static String campoRaiz(String campo) {
        int fimDoNome = campo.indexOf('[');
        return fimDoNome < 0 ? campo : campo.substring(0, fimDoNome);
    }

    @ExceptionHandler(CampoInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> tratarCampoInvalido(
            CampoInvalidoException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        null,
                        new ApiError("REQUISICAO_INVALIDA", ex.getMessage(), Map.of(ex.campo(), ex.getMessage()))
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> tratarCorpoIlegivel(
            HttpMessageNotReadableException ex
    ) {
        return erro(HttpStatus.BAD_REQUEST,
                "REQUISICAO_INVALIDA", "O corpo da requisição é inválido.");
    }

    @ExceptionHandler(EventoNaoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> tratarEventoNaoEncontrado(
            EventoNaoEncontradoException ex
    ) {
        return erro(HttpStatus.NOT_FOUND,
                "RECURSO_NAO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(EventoJaDecididoException.class)
    public ResponseEntity<ApiResponse<Void>> tratarEventoJaDecidido(
            EventoJaDecididoException ex
    ) {
        return erro(HttpStatus.CONFLICT,
                "EVENTO_JA_DECIDIDO", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> tratarRegraInvalida(
            IllegalArgumentException ex
    ) {
        return erro(HttpStatus.BAD_REQUEST,
                "REQUISICAO_INVALIDA", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> erro(
            HttpStatus status,
            String codigo,
            String mensagem
    ) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(
                        null,
                        new ApiError(codigo, mensagem)
                ));
    }

    @ExceptionHandler(EventoJaExistenteException.class)
    public ResponseEntity<ApiResponse<Void>> tratarEventoDuplicado(
            EventoJaExistenteException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(
                        null,
                        new ApiError("EVENTO_JA_EXISTENTE", ex.getMessage())
                ));
    }
}