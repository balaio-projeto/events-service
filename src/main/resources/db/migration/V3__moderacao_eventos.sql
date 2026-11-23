ALTER TABLE eventos
    ADD COLUMN organizador_id  UUID,
    ADD COLUMN submetido_em    TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN decidido_por    UUID,
    ADD COLUMN decidido_em     TIMESTAMPTZ,
    ADD COLUMN motivo_rejeicao TEXT;

ALTER TABLE eventos
    ADD CONSTRAINT chk_eventos_rejeicao_com_motivo
        CHECK (status_aprovacao <> 'REJEITADO'
            OR (motivo_rejeicao IS NOT NULL AND btrim(motivo_rejeicao) <> '')) NOT VALID,
    ADD CONSTRAINT chk_eventos_publicacao_exige_aprovacao
        CHECK (status_publicacao <> 'PUBLICADO' OR status_aprovacao = 'APROVADO') NOT VALID;

CREATE INDEX idx_eventos_moderacao ON eventos (status_aprovacao, submetido_em);
