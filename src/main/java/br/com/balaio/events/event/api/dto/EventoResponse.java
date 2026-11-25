
package br.com.balaio.events.event.api.dto;

import br.com.balaio.events.event.domain.DetalhesEvento;
import br.com.balaio.events.event.domain.Evento;

import java.util.List;
import java.util.Locale;

public record EventoResponse(
        String slug,
        String nome,
        String tipo,
        String organizador,
        String descricao,
        String periodo,
        String horario,
        String entrada,
        String cidade,
        String local,
        String endereco,
        String cep,
        String rua,
        String numero,
        String bairro,
        String complemento,
        String referencia,
        String site,
        Double lat,
        Double lng,
        List<String> artesaos,
        List<String> coletivos
) {

    public static EventoResponse fromDomain(Evento evento) {
        DetalhesEvento detalhes = evento.detalhes();
        return new EventoResponse(
                evento.slug(),
                evento.nome(),
                evento.tipo().toLowerCase(Locale.ROOT),
                evento.organizador(),
                evento.descricao(),
                evento.periodo(),
                evento.horario() == null ? "" : evento.horario(),
                evento.entrada(),
                evento.cidade(),
                evento.local(),
                evento.endereco(),
                detalhes.cep(),
                detalhes.rua(),
                detalhes.numero(),
                detalhes.bairro(),
                detalhes.complemento(),
                detalhes.referencia(),
                detalhes.site(),
                evento.latitude(),
                evento.longitude(),
                evento.artesaos(),
                evento.coletivos()
        );
    }
}
