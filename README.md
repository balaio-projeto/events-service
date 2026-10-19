# Events Service

Serviço dono dos eventos e da agenda cultural do Balaio, com dados para calendário e mapa.

## Estado atual

O repositório contém o esqueleto Spring Boot e dependências iniciais de persistência, validação e monitoramento. Não existem controllers, entidades ou migrações de negócio implementados. As rotas abaixo são planejadas a partir da [Fake API do frontend](https://github.com/lucaschavessf2/Balaio/tree/main/Frontend/fake-api).

## Responsabilidades

- Cadastrar, consultar e manter eventos culturais ligados ao artesanato de Pernambuco.
- Fornecer os dados usados nas telas de agenda, detalhe e mapa.
- Ordenar listagens por proximidade quando houver coordenadas `lat` e `lng`.
- Validar data, localização e dados do evento.
- Definir autorização de publicação/edição no backend real.

O serviço é dono dos dados de eventos; deve referenciar organizadores por ID de perfil e não consultar diretamente o banco de outro microsserviço.

## API planejada

Prefixo `/api/v1`. Nenhuma rota está implementada ainda.

| Método e rota | Uso |
| --- | --- |
| `GET /eventos?lat={lat}&lng={lng}` | Listagem; coordenadas permitem ordenar por distância |
| `GET /eventos/{slug}` | Detalhes do evento |
| `POST /eventos` | Publicar evento; permissão real ainda precisa ser definida |

A operação de publicação da Fake API não determina as regras de acesso do backend real.

## Contrato com o frontend

Preservar o prefixo `/api/v1` e o envelope `{ "dados": ..., "erro": null }`, usando a Fake API como referência de payloads. Documentar erros e casos sem resultados com status HTTP apropriado.

## Stack e desenvolvimento

- Java 25, Maven Wrapper, Spring Boot 4.1.1.
- Spring MVC, Spring Data JPA, Bean Validation, PostgreSQL, Flyway e Actuator.
- Requer JDK 25 e PostgreSQL. Use a infraestrutura do repositório `platform` quando disponível; configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` com a base própria do serviço. Não versione credenciais.
- Migrações em `src/main/resources/db/migration`, por exemplo `V1__create_events.sql`.
- O `application.yaml` atual só define o nome; datasource e migrações de negócio ainda precisam ser adicionados.

Windows: ` .\mvnw.cmd spring-boot:run`, ` .\mvnw.cmd test`, ` .\mvnw.cmd package`. Em Linux/macOS, use `./mvnw`.

Com Actuator, consulte `/actuator/health`. Os testes atuais só verificam contexto; acrescente cobertura de localização, datas e permissões.

## OpenAPI / Swagger UI

Ainda não está no `pom.xml`. A recomendação é adicionar OpenAPI e Swagger UI com os primeiros endpoints usando `org.springdoc:springdoc-openapi-starter-webmvc-ui` 3.x para Spring Boot 4 (versão estável consultada: 3.1.1). URLs esperadas após configurar: `/swagger-ui/index.html` e `/v3/api-docs`.

## Referências

- [Contrato da API do frontend](https://github.com/lucaschavessf2/Balaio/blob/main/Frontend/docs/api.md)
- [Fake API](https://github.com/lucaschavessf2/Balaio/tree/main/Frontend/fake-api)
- [springdoc-openapi](https://springdoc.org/)
