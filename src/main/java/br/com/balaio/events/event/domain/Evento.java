
package br.com.balaio.events.event.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record Evento(
        Long id,
        String slug,
        String nome,
        String tipo,
        String organizador,
        String descricao,
        StatusAprovacao statusAprovacao,
        StatusPublicacao statusPublicacao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String entrada,
        String cidade,
        String local,
        String endereco,
        Double latitude,
        Double longitude,
        String periodo,
        String horario,
        List<String> artesaos,
        List<String> coletivos,
        UUID organizadorId,
        Instant submetidoEm,
        UUID decididoPor,
        Instant decididoEm,
        String motivoRejeicao
) {

    public static final int LIMITE_MOTIVO_REJEICAO = 1000;

    public Evento(
            Long id,
            String slug,
            String nome,
            String tipo,
            String organizador,
            String descricao,
            StatusAprovacao statusAprovacao,
            StatusPublicacao statusPublicacao,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horarioInicio,
            LocalTime horarioFim,
            String entrada,
            String cidade,
            String local,
            String endereco,
            Double latitude,
            Double longitude,
            String periodo,
            String horario,
            List<String> artesaos,
            List<String> coletivos
    ) {
        this(id, slug, nome, tipo, organizador, descricao, statusAprovacao, statusPublicacao,
                dataInicio, dataFim, horarioInicio, horarioFim, entrada, cidade, local, endereco,
                latitude, longitude, periodo, horario, artesaos, coletivos,
                null, null, null, null, null);
    }

    public Evento {
        artesaos = artesaos == null
                ? List.of()
                : List.copyOf(artesaos);

        coletivos = coletivos == null
                ? List.of()
                : List.copyOf(coletivos);

        if ((dataInicio == null) != (dataFim == null)) {
            throw new IllegalArgumentException(
                    "Datas inicial e final devem ser informadas juntas."
            );
        }

        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException(
                    "Data final anterior à data inicial."
            );
        }

        if (latitude == null || longitude == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Coordenadas geográficas inválidas."
            );
        }
    }

    public boolean isPublicamenteVisivel() {
        return statusAprovacao == StatusAprovacao.APROVADO
                && statusPublicacao == StatusPublicacao.PUBLICADO;
    }

    public boolean isPendente() {
        return statusAprovacao == StatusAprovacao.PENDENTE;
    }

    public Evento submetidoPor(UUID contaOrganizadora, Instant quando) {
        if (contaOrganizadora == null || quando == null) {
            throw new IllegalArgumentException("A submissão exige organizador e data.");
        }
        return new Evento(null, slug, nome, tipo, organizador, descricao,
                StatusAprovacao.PENDENTE, StatusPublicacao.NAO_PUBLICADO,
                dataInicio, dataFim, horarioInicio, horarioFim, entrada, cidade, local, endereco,
                latitude, longitude, periodo, horario, artesaos, coletivos,
                contaOrganizadora, quando, null, null, null);
    }

    public Evento aprovadoPor(UUID administrador, Instant quando) {
        exigirPendente();
        return decidido(StatusAprovacao.APROVADO, StatusPublicacao.PUBLICADO, administrador, quando, null);
    }

    public Evento rejeitadoPor(UUID administrador, Instant quando, String motivo) {
        exigirPendente();
        String motivoLimpo = motivo == null ? "" : motivo.strip();
        if (motivoLimpo.isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo da rejeição.");
        }
        if (motivoLimpo.length() > LIMITE_MOTIVO_REJEICAO) {
            throw new IllegalArgumentException("O motivo da rejeição deve ter até 1000 caracteres.");
        }
        return decidido(StatusAprovacao.REJEITADO, StatusPublicacao.NAO_PUBLICADO, administrador, quando, motivoLimpo);
    }

    private void exigirPendente() {
        if (!isPendente()) {
            throw new IllegalStateException("Somente eventos pendentes podem ser decididos.");
        }
    }

    private Evento decidido(
            StatusAprovacao aprovacao,
            StatusPublicacao publicacao,
            UUID administrador,
            Instant quando,
            String motivo
    ) {
        if (administrador == null || quando == null) {
            throw new IllegalArgumentException("A decisão exige responsável e data.");
        }
        return new Evento(id, slug, nome, tipo, organizador, descricao, aprovacao, publicacao,
                dataInicio, dataFim, horarioInicio, horarioFim, entrada, cidade, local, endereco,
                latitude, longitude, periodo, horario, artesaos, coletivos,
                organizadorId, submetidoEm, administrador, quando, motivo);
    }
}
