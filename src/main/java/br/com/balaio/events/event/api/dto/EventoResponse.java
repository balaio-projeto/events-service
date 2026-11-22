
package br.com.balaio.events.event.api.dto;

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
        Double lat,
        Double lng,
        List<String> artesaos,
        List<String> coletivos
) {

    public static EventoResponse fromDomain(Evento evento) {
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
                evento.latitude(),
                evento.longitude(),
                evento.artesaos(),
                evento.coletivos()
        );
    }
}
