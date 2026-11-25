package br.com.balaio.events.event.domain;

public record DetalhesEvento(
        String cep,
        String rua,
        String numero,
        String bairro,
        String complemento,
        String referencia,
        String site,
        String contatoNome,
        String contatoTelefone,
        String contatoEmail
) {

    public static final DetalhesEvento VAZIO =
            new DetalhesEvento(null, null, null, null, null, null, null, null, null, null);

    private static final String CEP_SOMENTE_DIGITOS = "\\d{8}";

    public DetalhesEvento {
        cep = cepNormalizado(textoOuNulo(cep));
        rua = textoOuNulo(rua);
        numero = textoOuNulo(numero);
        bairro = textoOuNulo(bairro);
        complemento = textoOuNulo(complemento);
        referencia = textoOuNulo(referencia);
        site = textoOuNulo(site);
        contatoNome = textoOuNulo(contatoNome);
        contatoTelefone = textoOuNulo(contatoTelefone);
        contatoEmail = textoOuNulo(contatoEmail);
    }

    private static String textoOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }

    private static String cepNormalizado(String cep) {
        if (cep == null) {
            return null;
        }
        String digitos = cep.replace("-", "");
        if (!digitos.matches(CEP_SOMENTE_DIGITOS)) {
            return cep;
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }
}
