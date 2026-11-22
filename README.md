# events-service

Eventos, calendário e localização. O serviço é independente, usa Java 25 e Spring Boot 4.1.1 e recebe requisições externas por meio do api-gateway.

## Estado atual

- **Branch `develop`:** sem controllers. Não há endpoints HTTP expostos, e `/v3/api-docs` não lista caminhos.
- **Branch `PI-199`:** `GET /api/v1/eventos` e `GET /api/v1/eventos/{slug}`, com consulta por distância. Esses endpoints só existem após o merge do PI-199.

## Rotas (após o PI-199)

| Método | Caminho | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/v1/eventos` | Público | Lista eventos publicados. Aceita `lat` e `lng` opcionais para consulta por distância. Coordenadas inválidas retornam 400. |
| GET | `/api/v1/eventos/{slug}` | Público | Detalhe de um evento. |

A especificação OpenAPI do serviço fica em `http://localhost:8085/v3/api-docs`, e a interface em `http://localhost:8085/swagger-ui.html`.

**Atenção:** a política do gateway (`GatewayAccessPolicy`) marca `POST /api/v1/eventos` como público. Nenhum branch tem endpoint de criação, mas essa regra deve ser revisada antes de existir um endpoint de escrita.

## Variáveis

| Variável | Padrão | Descrição |
|---|---|---|
| `EVENTS_SERVICE_PORT` | `8085` | Porta HTTP. Opcional. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://events-db:5432/balaio_events` | Banco próprio do serviço. |
| `SPRING_DATASOURCE_USERNAME` | — | Obrigatória. Definida pelo `platform`. |
| `SPRING_DATASOURCE_PASSWORD` | — | Obrigatória. Definida pelo `platform`. |

## Execução

Para executar pela IDE, defina as variáveis acima. O banco e suas credenciais são configurados no repositório `platform`.

No Windows: `.\mvnw.cmd spring-boot:run`. Testes: `.\mvnw.cmd test`.

Os testes de contexto (`EventsServiceApplicationTests`) precisam de um PostgreSQL acessível com as variáveis acima. O CI sobe um serviço PostgreSQL para isso.

O `Dockerfile` constrói uma imagem própria. Em `platform`, use `docker compose up --build -d events-service` para iniciar este serviço e seu banco, ou `docker compose --profile extended up --build -d` para iniciar todos.

Health: `http://localhost:8085/actuator/health`.

## Migrações

Migrações SQL versionadas ficam em `src/main/resources/db/migration`.
