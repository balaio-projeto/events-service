package br.com.balaio.events.event.application.Exception;

public class EventoJaDecididoException extends RuntimeException {
    public EventoJaDecididoException() {
        super("Este evento já recebeu outra decisão.");
    }
}
