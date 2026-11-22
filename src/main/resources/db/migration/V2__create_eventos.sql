
-- Campos de apresentação e referências externas.
ALTER TABLE eventos
    ADD COLUMN periodo TEXT,
    ADD COLUMN horario TEXT,
    ADD COLUMN artesaos JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN coletivos JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Preserva os eventos existentes na V1.
UPDATE eventos
SET periodo = CASE
        WHEN data_inicio = data_fim
        THEN to_char(data_inicio, 'DD/MM/YYYY')
        ELSE
            to_char(data_inicio, 'DD/MM/YYYY')
                || ' a '
                || to_char(data_fim, 'DD/MM/YYYY')
        END,
    horario = CASE
        WHEN horario_inicio IS NULL THEN ''
        WHEN horario_fim IS NULL
        THEN to_char(horario_inicio, 'HH24:MI')
        ELSE
            to_char(horario_inicio, 'HH24:MI')
            || ' às '
            || to_char(horario_fim, 'HH24:MI')
        END;

ALTER TABLE eventos
ALTER COLUMN periodo SET NOT NULL,
ALTER COLUMN data_inicio DROP NOT NULL,
ALTER COLUMN data_fim DROP NOT NULL;
