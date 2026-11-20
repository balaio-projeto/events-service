package br.com.balaio.events.event.api.Controller;

import br.com.balaio.events.event.api.dto.ApiResponse;
import br.com.balaio.events.event.api.dto.CriarEventoRequest;
import br.com.balaio.events.event.api.dto.EventoResponse;
import br.com.balaio.events.event.application.usecase.ConsultarEventoPorSlugUseCase;
import br.com.balaio.events.event.application.usecase.ConsultarEventosPublicosUseCase;
import br.com.balaio.events.event.application.usecase.CriarEventoUseCase;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    private final ConsultarEventosPublicosUseCase useCase;
    private final ConsultarEventoPorSlugUseCase consultarPorSlugUseCase;
    private final CriarEventoUseCase criarEventoUseCase;

    public EventoController(
            ConsultarEventosPublicosUseCase useCase,
            ConsultarEventoPorSlugUseCase consultarPorSlugUseCase,
            CriarEventoUseCase criarEventoUseCase
    ) {
        this.useCase = useCase;
        this.consultarPorSlugUseCase = consultarPorSlugUseCase;
        this.criarEventoUseCase = criarEventoUseCase;
    }

    @GetMapping
    public ApiResponse<List<EventoResponse>> listarEventos(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng)
    {
        List<Evento> eventos;

        try {
            eventos = useCase.executar(lat, lng);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage(),
                    ex
            );
        }
        List<EventoResponse> respostas = eventos.stream()
                .map(EventoResponse::fromDomain)
                .toList();

        return new ApiResponse<>(respostas, null);
    }

    @GetMapping("/{slug}")
    public ApiResponse<EventoResponse> consultarPorSlug(@PathVariable String slug){
        Evento evento = consultarPorSlugUseCase.executar(slug).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Evento não encontrado"
        ));

        return new ApiResponse<>(EventoResponse.fromDomain(evento),null);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventoResponse>> criarEvento(
            @RequestBody CriarEventoRequest request
    ) {

        Evento novoEvento = new Evento(
                null,
                request.slug(),
                request.nome(),
                request.tipo(),
                request.organizador(),
                request.descricao(),
                StatusAprovacao.PENDENTE,
                StatusPublicacao.NAO_PUBLICADO,
                request.dataInicio(),
                request.dataFim(),
                request.horarioInicio(),
                request.horarioFim(),
                request.entrada(),
                request.cidade(),
                request.local(),
                request.endereco(),
                request.latitude(),
                request.longitude()
        );

        Evento eventoSalvo = criarEventoUseCase.executar(novoEvento);

        EventoResponse response = EventoResponse.fromDomain(eventoSalvo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(response, null));
    }
}
