package br.com.balaio.events.event.application.Exception;

public class CampoInvalidoException extends RuntimeException {
    private final String campo;

    public CampoInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String campo() {
        return campo;
    }
}
