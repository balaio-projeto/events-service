
package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.DetalhesEvento;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CriarEventoRequest(

        @NotBlank(message = "Não foi possível gerar o endereço do evento.")
        @Size(max = 255, message = "O nome do evento ficou longo demais.")
        String slug,

        @NotBlank(message = "Dê um nome ao evento.")
        @Size(max = 255, message = "Use até 255 caracteres no nome.")
        String nome,

        @NotBlank(message = "Escolha o tipo de evento.")
        @Pattern(
                regexp = "feira|festival|exposicao|oficina",
                message = "Escolha feira, festival, exposição ou oficina."
        )
        String tipo,

        @NotBlank(message = "Diga quem organiza o evento.")
        @Size(max = 255, message = "Use até 255 caracteres para quem organiza.")
        String organizador,

        @NotBlank(message = "Conte o que vai acontecer no evento.")
        String descricao,

        @NotBlank(message = "Informe as datas do evento.")
        String periodo,

        String horario,

        @NotBlank(message = "Informe a entrada, como Gratuita.")
        String entrada,

        @NotBlank(message = "Escolha o município do evento.")
        @Size(max = 150, message = "Use até 150 caracteres no município.")
        String cidade,

        @NotBlank(message = "Informe o local do evento.")
        @Size(max = 255, message = "Use até 255 caracteres no local.")
        String local,

        @NotBlank(message = "Informe o endereço do evento.")
        @Size(max = 500, message = "Use até 500 caracteres no endereço.")
        String endereco,

        @Pattern(regexp = "\\s*(\\d{5}-?\\d{3})?\\s*", message = "Informe o CEP no formato 00000-000.")
        String cep,

        @NotBlank(message = "Informe a rua do evento.")
        @Size(max = 200, message = "Use até 200 caracteres na rua.")
        String rua,

        @NotBlank(message = "Informe o número do local ou S/N.")
        @Size(max = 20, message = "Use até 20 caracteres no número.")
        String numero,

        @NotBlank(message = "Informe o bairro do evento.")
        @Size(max = 120, message = "Use até 120 caracteres no bairro.")
        String bairro,

        @Size(max = 120, message = "Use até 120 caracteres no complemento.")
        String complemento,

        @Size(max = 200, message = "Use até 200 caracteres no ponto de referência.")
        String referencia,

        @Size(max = 300, message = "Use até 300 caracteres no link do evento.")
        @Pattern(
                regexp = "\\s*(https?://[^\\s/?#]+\\S*)?\\s*",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Informe um link começando com http:// ou https://."
        )
        String site,

        @NotBlank(message = "Informe o nome de quem podemos contatar.")
        @Size(max = 120, message = "Use até 120 caracteres no nome do contato.")
        String contatoNome,

        @NotBlank(message = "Informe um telefone de contato.")
        @Size(max = 20, message = "Use até 20 caracteres no telefone.")
        @Pattern(
                regexp = "\\s*\\+?(?=(?:\\D*\\d){10})[\\d\\s().-]+",
                message = "Informe um telefone com DDD, com pelo menos 10 dígitos."
        )
        String contatoTelefone,

        @Size(max = 160, message = "Use até 160 caracteres no e-mail.")
        @Email(regexp = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", message = "Informe um e-mail válido.")
        String contatoEmail,

        @NotNull(message = "Marque o ponto do evento no mapa.")
        @DecimalMin(value = "-90.0", message = "Marque um ponto válido no mapa.")
        @DecimalMax(value = "90.0", message = "Marque um ponto válido no mapa.")
        Double lat,

        @NotNull(message = "Marque o ponto do evento no mapa.")
        @DecimalMin(value = "-180.0", message = "Marque um ponto válido no mapa.")
        @DecimalMax(value = "180.0", message = "Marque um ponto válido no mapa.")
        Double lng,

        @NotNull(message = "Informe os artesãos participantes.")
        List<@NotBlank(message = "Participante inválido.") String> artesaos,

        @NotNull(message = "Informe os coletivos participantes.")
        List<@NotBlank(message = "Participante inválido.") String> coletivos,

        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,

        Boolean criadoPorVoce
) {

    public DetalhesEvento paraDetalhesEvento() {
        return new DetalhesEvento(cep, rua, numero, bairro, complemento, referencia, site,
                contatoNome, contatoTelefone, contatoEmail);
    }
}
