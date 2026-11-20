package br.com.balaio.events.event.api.execption;

import br.com.balaio.events.event.api.dto.ApiError;
import br.com.balaio.events.event.api.dto.ApiResponse;
import br.com.balaio.events.event.application.Exception.EventoJaExistenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

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

        return erro(HttpStatus.BAD_REQUEST,
                "REQUISICAO_INVALIDA", mensagem);
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