package br.com.balaio.events.event.application.Exception;

public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException() {
        super("Evento não encontrado.");
    }
}
