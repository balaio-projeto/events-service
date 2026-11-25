ALTER TABLE eventos
    ADD COLUMN cep              VARCHAR(9),
    ADD COLUMN rua              VARCHAR(200),
    ADD COLUMN numero           VARCHAR(20),
    ADD COLUMN bairro           VARCHAR(120),
    ADD COLUMN complemento      VARCHAR(120),
    ADD COLUMN referencia       VARCHAR(200),
    ADD COLUMN site             VARCHAR(300),
    ADD COLUMN contato_nome     VARCHAR(120),
    ADD COLUMN contato_telefone VARCHAR(20),
    ADD COLUMN contato_email    VARCHAR(160);
