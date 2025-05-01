# AutoHub - Vehicles API

API responsável pelo gerenciamento do ciclo de vida dos veículos na plataforma AutoHub, incluindo cadastro, consulta,
atualização, reserva durante o processo de venda e atualização de status final (vendido ou retorno para disponível).

## Índice

* [Visão Geral](#visão-geral)
* [Arquitetura](#arquitetura)
* [Tecnologias](#tecnologias)
* [Configuração](#configuração)
    * [Variáveis de Ambiente](#variáveis-de-ambiente)
    * [Ficheiros de Configuração](#ficheiros-de-configuração)
* [Executando Localmente](#executando-localmente)
    * [Com Docker Compose](#com-docker-compose)
* [Testes](#testes)
    * [Testes Locais End-to-End](#testes-locais-end-to-end)
* [API Endpoints (HTTP)](#api-endpoints-http)
* [Eventos Consumidos (SQS)](#eventos-consumidos-sqs)
* [Eventos Publicados (SNS)](#eventos-publicados-sns)
* [Modelo de Dados](#modelo-de-dados)
* [Deployment (AWS Lambda)](#deployment-aws-lambda)

## Visão Geral

Esta API gerencia o inventário de veículos da plataforma. Suas principais responsabilidades são:

* Permitir que usuários autenticados cadastrem (`POST /vehicles`) e atualizem (`PUT /vehicles/{id}`) os dados dos seus
  veículos à venda.
* Expor endpoints públicos para listar veículos disponíveis (`GET /vehicles/available`) e vendidos (
  `GET /vehicles/sold`), com filtros.
* Permitir a consulta de um veículo específico por ID (`GET /vehicles/{id}`).
* Permitir que usuários autenticados vejam seus próprios veículos (`GET /vehicles/my-vehicles`) e os removam
  logicamente (`DELETE /vehicles/{id}`).
* Receber o evento `SaleCreated` para tentar reservar um veículo (`RESERVED`).
* Publicar eventos `VehicleReserved` (sucesso) ou `VehicleReservationFailed` (falha).
* Receber eventos de resultado de pagamento (`PaymentCompleted`, `PaymentFailed`, `ChargeCreationFailed`,
  `ChargeExpired`) para atualizar o status final do veículo (`SOLD` em caso de sucesso, `AVAILABLE` em caso de
  falha/expiração).

## Arquitetura

A API segue a **Arquitetura Hexagonal**, separando o domínio de negócio da infraestrutura.

* **Domínio:** Contém as entidades (`Vehicle`, `VehicleStatus`), eventos (`VehicleReserved`,
  `VehicleReservationFailed`), exceções e as interfaces das portas (entrada: `VehicleServicePort`; saída:
  `VehicleRepositoryPort`, `VehicleEventPublisherPort` - implementado via `SNSEventPublisher`).
* **Aplicação:** Contém a implementação da lógica de negócio (`VehicleServiceImpl`).
* **Infraestrutura:** Contém os adaptadores:
    * **Entrada:** Controller REST (`VehicleController`); Consumidor SQS (`VehicleEventConsumer`) para eventos da saga.
    * **Saída:** Adaptador de persistência para PostgreSQL (`PostgresVehicleRepositoryAdapter`), Adaptador de publicação
      para SNS (`SNSEventPublisher`).
* **Deployment:** A aplicação é empacotada como um "fat JAR" e deployada em duas funções AWS Lambda distintas:
    * **Lambda HTTP:** Acionada pelo API Gateway, usa `StreamLambdaHandler` (`aws-serverless-java-container`).
      Responsável pelos endpoints REST.
    * **Lambda SQS:** Acionada por Event Source Mapping de uma fila SQS unificada, usa `FunctionInvoker` (
      `spring-cloud-function-adapter-aws`). Responsável por processar eventos da saga (`SaleCreated`, eventos de
      pagamento).

## Tecnologias

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.4.4
* **Build:** Maven
* **Base de Dados:** AWS RDS PostgreSQL
* **Mensageria:** AWS SNS, AWS SQS (via Spring Cloud AWS / Spring Cloud Function)
* **Infraestrutura:** AWS Lambda, API Gateway, Terraform, AWS Secrets Manager
* **Testes:** JUnit 5, Mockito, Testcontainers (PostgreSQL, LocalStack)
* **Documentação:** Springdoc OpenAPI (Swagger UI)
* **Outros:** MapStruct, Spring Data JPA, Hibernate

## Configuração

A configuração da aplicação é gerenciada através de perfis Spring e ficheiros `application*.yml`.

### Variáveis de Ambiente

As seguintes variáveis de ambiente são esperadas, especialmente no ambiente AWS (configuradas via Terraform):

* `SPRING_PROFILES_ACTIVE`: Define os perfis ativos (ex: `prod,http` ou `prod,sqs`).
* `AWS_REGION`: Região AWS onde a aplicação está a correr.
* `DB_HOST`: Endpoint do RDS PostgreSQL.
* `DB_PORT`: Porta do RDS PostgreSQL.
* `DB_NAME`: Nome do banco de dados no RDS.
* `DB_USER`: Usuário master do RDS.
* `DB_PASSWORD_SECRET_ARN`: ARN do segredo no Secrets Manager contendo a senha do DB.
* `SNS_TOPIC_MAIN_EVENT_BUS_ARN`: ARN do tópico SNS principal.
* `SQS_QUEUE_VEHICLES_EVENTS_NAME`: Nome da fila SQS unificada para eventos da Vehicles API.
* `SPRING_CLOUD_FUNCTION_DEFINITION`: (Apenas para Lambda SQS) Nome do bean `@Bean Consumer<SQSEvent>` (ex:
  `vehicleEventsConsumer`).
* `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: (Apenas para Lambda HTTP) URI do emissor JWT para validação de
  token.

### Ficheiros de Configuração

* `application.yml`: Configurações base, defaults para ambiente local, placeholders.
* `application-prod.yml`: Configurações comuns de produção (JPA, Flyway desabilitado, região AWS, nível de log). Define
  as propriedades AWS/DB para ler das variáveis de ambiente.
* `application-http.yml`: Ativado com perfil `http`. Exclui auto-configurações SQS/Function.
* `application-sqs.yml`: Ativado com perfil `sqs`. Define `web-application-type: none`, exclui auto-configurações
  Web/Security/Swagger, define `spring.cloud.function.definition`.
* `application-local.yml`: Ativado com perfil `local`. Aponta para endpoints do LocalStack e Postgres do Docker Compose,
  define credenciais dummy, nomes/URLs locais.
* `application-test.yml`: Ativado com perfil `test`. Exclui `SecretsManager`, configura DataSource H2 (ou para
  Testcontainers), desabilita Flyway, define `issuer-uri` dummy.

## Executando Localmente

### Com Docker Compose

1. Certifique-se de que Docker e Docker Compose estão correndo.
2. Navegue até ao diretório que contém o `docker-compose.yml` (que deve incluir serviços para Postgres e LocalStack).
3. Inicie os serviços: `docker-compose up -d`.
4. Use `awslocal` para criar os recursos necessários no LocalStack (tópico SNS, filas SQS, assinaturas) conforme
   definido no `messaging/main.tf` e `messaging/subscriptions.tf` (ou use um script de inicialização como o
   `init-aws.sh`).
5. Inicie a aplicação Spring Boot com os perfis apropriados:
    * Para testar a API/Swagger: `-Dspring.profiles.active=local,http`
    * Para testar o consumidor SQS: `-Dspring.profiles.active=local,sqs`

## Testes

### Testes Locais End-to-End

1. Inicie a aplicação com perfis `local,http`.
2. Use o Swagger UI (`http://localhost:8080/swagger-ui.html`) ou `curl` para criar/atualizar/consultar veículos. Use um
   JWT de teste para endpoints protegidos.
3. Inicie a aplicação com perfis `local,sqs`.
4. Use `awslocal sqs send-message` para enviar eventos simulados (`SaleCreated`, `PaymentCompleted`, etc.) para a fila
   `VehiclesApi_Events_Queue-local`.
5. Verifique os logs da aplicação e o estado do veículo no banco de dados Postgres local. Verifique se os eventos
   `VehicleReserved` ou `VehicleReservationFailed` são publicados no SNS do LocalStack.

## API Endpoints (HTTP)

* **Swagger UI:** `http://localhost:8080/swagger-ui.html` (quando a correr com perfil `http`)

| Método | Path                    | Autenticação | Descrição                         |
|:-------|:------------------------|:-------------|:----------------------------------|
| POST   | `/vehicles`             | JWT Bearer   | Cadastra um novo veículo.         |
| PUT    | `/vehicles/{id}`        | JWT Bearer   | Atualiza um veículo existente.    |
| GET    | `/vehicles/available`   | Nenhuma      | Lista veículos disponíveis.       |
| GET    | `/vehicles/sold`        | Nenhuma      | Lista veículos vendidos.          |
| GET    | `/vehicles/{id}`        | Nenhuma      | Busca detalhes de um veículo.     |
| GET    | `/vehicles/my-vehicles` | JWT Bearer   | Lista veículos do usuário logado. |
| DELETE | `/vehicles/{id}`        | JWT Bearer   | Remove (logicamente) um veículo.  |

## Eventos Consumidos (SQS)

A Lambda SQS (`AutoHubVehiclesApiSqs-{env}`) consome da fila unificada `VehiclesApi_Events_Queue-{env}`:

| EventType              | Publicado Por                            | Descrição                     | Ação na Vehicles API                       |
|:-----------------------|:-----------------------------------------|:------------------------------|:-------------------------------------------|
| `SaleCreated`          | `sales-api` (via SNS)                    | Início de uma nova venda.     | Tenta reservar o veículo (`RESERVED`).     |
| `PaymentCompleted`     | `charges-api` (via SNS)                  | Pagamento confirmado.         | Marca o veículo como `SOLD`.               |
| `PaymentFailed`        | `charges-api` (via SNS)                  | Pagamento falhou.             | Libera a reserva do veículo (`AVAILABLE`). |
| `ChargeCreationFailed` | `charges-api` (via SNS)                  | Falha na criação da cobrança. | Libera a reserva do veículo (`AVAILABLE`). |
| `ChargeExpired`        | `charges-api`/`timeout-lambda` (via SNS) | Cobrança expirou.             | Libera a reserva do veículo (`AVAILABLE`). |

## Eventos Publicados (SNS)

Esta API publica os seguintes eventos no tópico SNS `AutoHubBusinessEventsTopic-{env}`:

| EventType                  | Disparado Por                               | Descrição                                       |
|:---------------------------|:--------------------------------------------|:------------------------------------------------|
| `VehicleReserved`          | Sucesso ao reservar o veículo (`RESERVED`). | Notifica que a reserva foi feita.               |
| `VehicleReservationFailed` | Falha ao tentar reservar o veículo.         | Notifica falha na reserva (status, lock, etc.). |

## Modelo de Dados

* **Base de Dados:** AWS RDS PostgreSQL
* **Tabelas Principais:**
    * `vehicles`: Armazena os dados dos veículos (id, make, model, year, color, price, description, status, owner_id,
      version, created_at, updated_at).
    * `vehicle_audit_log`: Guarda snapshots JSON do histórico de alterações dos veículos.
* **Migrações:** Gerenciadas via Flyway (scripts em `src/main/resources/db/migration`).

## Deployment (AWS Lambda)

* **Deploy:** Realizado via pipeline GitHub Actions (`.github/workflows/cicd-vehicles.yml`).
* **Artefacto:** Um único "fat JAR" com classifier `-aws.jar` gerado pelo `maven-shade-plugin`.
* **Funções:**
    * `AutoHubVehiclesApiHttp-{env}`:
        * **Trigger:** API Gateway.
        * **Handler:** `com.fiap.autohub.autohub_vehicles_api.application.config.StreamLambdaHandler`.
        * **Perfis Ativos:** `prod,http`.
    * `AutoHubVehiclesApiSqs-{env}`:
        * **Trigger:** Event Source Mapping da fila `VehiclesApi_Events_Queue-{env}`.
        * **Handler:** `org.springframework.cloud.function.adapter.aws.FunctionInvoker`.
        * **Perfis Ativos:** `prod,sqs`.
        * **Variável `SPRING_CLOUD_FUNCTION_DEFINITION`:** `vehicleEventsConsumer`.
* **Variáveis de Ambiente:** Consultar a seção [Variáveis de Ambiente](#variáveis-de-ambiente).

