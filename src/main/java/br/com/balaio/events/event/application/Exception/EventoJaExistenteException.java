package br.com.balaio.events.event.application.Exception;

public class EventoJaExistenteException extends RuntimeException {
    public EventoJaExistenteException() {
        super("Já existe um evento com esse slug.");
    }
}
