package br.com.balaio.events.event.api.Controller;

import br.com.balaio.events.event.api.dto.ApiResponse;
import br.com.balaio.events.event.api.dto.DecisaoEventoRequest;
import br.com.balaio.events.event.api.dto.EventoModeracaoResponse;
import br.com.balaio.events.event.application.Exception.CampoInvalidoException;
import br.com.balaio.events.event.application.Exception.EventoNaoEncontradoException;
import br.com.balaio.events.event.application.usecase.DecidirEventoUseCase;
import br.com.balaio.events.event.application.usecase.IdentificarSolicitantesUseCase;
import br.com.balaio.events.event.application.usecase.ListarEventosEmModeracaoUseCase;
import br.com.balaio.events.event.domain.DecisaoModeracao;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.Solicitante;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.security.ChamadorAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/eventos")
public class AdminEventoController {

    private final ListarEventosEmModeracaoUseCase listarUseCase;
    private final DecidirEventoUseCase decidirUseCase;
    private final IdentificarSolicitantesUseCase identificarSolicitantesUseCase;

    public AdminEventoController(
            ListarEventosEmModeracaoUseCase listarUseCase,
            DecidirEventoUseCase decidirUseCase,
            IdentificarSolicitantesUseCase identificarSolicitantesUseCase
    ) {
        this.listarUseCase = listarUseCase;
        this.decidirUseCase = decidirUseCase;
        this.identificarSolicitantesUseCase = identificarSolicitantesUseCase;
    }

    @GetMapping
    public ApiResponse<List<EventoModeracaoResponse>> listar(
            @RequestParam(defaultValue = "PENDENTE") String estado
    ) {
        List<Evento> eventos = listarUseCase.executar(estadoDe(estado));
        Map<UUID, Solicitante> solicitantes = identificarSolicitantesUseCase.identificar(eventos);
        List<EventoModeracaoResponse> dados = eventos.stream()
                .map(evento -> EventoModeracaoResponse.fromDomain(evento, solicitantes.get(evento.organizadorId())))
                .toList();
        return new ApiResponse<>(dados, null);
    }

    @GetMapping("/{slug}")
    public ApiResponse<EventoModeracaoResponse> detalhar(@PathVariable String slug) {
        Evento evento = listarUseCase.detalhar(slug).orElseThrow(EventoNaoEncontradoException::new);
        return new ApiResponse<>(paraModeracao(evento), null);
    }

    @PostMapping("/{slug}/decisao")
    public ApiResponse<EventoModeracaoResponse> decidir(
            @PathVariable String slug,
            @Valid @RequestBody DecisaoEventoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        DecisaoModeracao decisao = DecisaoModeracao.de(request.decisao())
                .orElseThrow(() -> new CampoInvalidoException("decisao", "Escolha aprovar ou rejeitar."));
        Evento decidido = decidirUseCase.executar(
                slug,
                decisao,
                request.motivo(),
                ChamadorAutenticado.de(jwt).contaId()
        );
        return new ApiResponse<>(paraModeracao(decidido), null);
    }

    private EventoModeracaoResponse paraModeracao(Evento evento) {
        return EventoModeracaoResponse.fromDomain(evento, identificarSolicitantesUseCase.identificar(evento));
    }

    private static StatusAprovacao estadoDe(String valor) {
        String normalizado = valor.strip().toUpperCase(Locale.ROOT);
        return Arrays.stream(StatusAprovacao.values())
                .filter(status -> status.name().equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new CampoInvalidoException(
                        "estado", "Use PENDENTE, APROVADO ou REJEITADO."));
    }
}
