# Credit Limit Service

Microservice responsável pelo cálculo, consulta e gerenciamento de limites de crédito dentro da **Credit Journey Platform**.

Doc do sistema completo:

```text
https://github.com/diogobackend/credit-journey-platform
```

Este serviço representa o contexto de limite de crédito em uma plataforma fictícia de banco digital.

Ele será responsável por criar, consultar, calcular, atualizar, bloquear e liberar limites de crédito associados a um cliente.

---

## Responsabilidade do serviço

O `credit-limit-service` é responsável por:

- criar limite de crédito;
- consultar limite por cliente;
- calcular limite inicial;
- atualizar limite;
- bloquear limite;
- liberar limite;
- registrar histórico de alterações;
- validar regras de domínio;
- expor endpoints REST;
- versionar banco com Flyway;
- expor endpoints operacionais com Actuator;
- gerar logs automáticos nos use cases futuramente;
- publicar eventos de domínio relacionados a limite.

Exemplo prático da jornada:

```text
Cliente cadastrado
      |
      v
Rules Engine aprova elegibilidade
      |
      v
Limit Service calcula limite
      |
      v
Communication Service notifica o cliente
      |
      v
Audit Service registra a jornada
```

---

## Stack técnica

- Kotlin
- Java 21
- Spring Boot 4
- Gradle Kotlin DSL
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Flyway
- MySQL
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger
- Docker Compose
- MockK
- AssertJ
- JaCoCo
- ktlint

---

# Arquitetura

Este serviço segue **Arquitetura Hexagonal / Ports and Adapters**.

Regra principal:

```text
O domínio não deve depender de Spring, banco de dados, HTTP, mensageria ou qualquer detalhe de infraestrutura.
```

---

## Estrutura base

```text
src/main/kotlin/com/creditjourney/limit/
├── CreditLimitServiceApplication.kt
├── core/
│   ├── common/
│   │   └── messages/
│   ├── domain/
│   │   ├── model/
│   │   ├── exception/
│   │   └── valueobject/
│   ├── port/
│   │   ├── input/
│   │   └── output/
│   └── usecase/
└── app/
    ├── adapter/
    │   ├── input/
    │   │   ├── messaging/
    │   │   └── web/
    │   │       ├── controllers/
    │   │       ├── handler/
    │   │       ├── mappers/
    │   │       ├── requests/
    │   │       ├── responses/
    │   │       └── swagger/
    │   └── output/
    │       ├── messaging/
    │       └── persistence/
    │           ├── entity/
    │           ├── mapper/
    │           └── repository/
    └── configuration/
        └── logs/
```

---

## Responsabilidade das camadas

### core/domain

Contém os modelos e regras centrais do domínio.

Exemplos:

```text
CreditLimit
LimitStatus
Money
```

Essa camada não deve conhecer Spring, JPA, DTOs, controllers, banco de dados ou qualquer detalhe de infraestrutura.

---

### core/port/input

Define o que a aplicação sabe fazer.

Exemplos:

```text
CreateCreditLimitPort
FindCreditLimitByCustomerIdPort
CalculateInitialLimitPort
UpdateCreditLimitPort
BlockCreditLimitPort
ReleaseCreditLimitPort
```

Controllers devem chamar portas de entrada.

---

### core/port/output

Define o que a aplicação precisa acessar fora do domínio.

Exemplo:

```text
CreditLimitRepositoryPort
```

Use cases dependem dessas portas, não de repositories Spring Data diretamente.

---

### core/usecase

Contém a implementação dos casos de uso.

Exemplos:

```text
CreateCreditLimitUseCase
FindCreditLimitByCustomerIdUseCase
CalculateInitialLimitUseCase
UpdateCreditLimitUseCase
BlockCreditLimitUseCase
ReleaseCreditLimitUseCase
```

Aqui ficam as regras de aplicação.

---

### app/adapter/input/web

Camada de entrada HTTP.

Contém:

```text
controllers
requests
responses
mappers
handler
swagger
```

O controller não executa regra de negócio diretamente.

Ele recebe a requisição, valida os dados, converte o payload e chama uma porta de entrada.

---

### app/adapter/output/persistence

Camada de persistência.

Contém:

```text
entities JPA
repositories Spring Data
mappers Entity <-> Domain
adapter de persistência
```

A entity JPA não deve ser usada como modelo de domínio.

---

### app/configuration

Contém configurações Spring.

Exemplos:

```text
UseCaseConfiguration
LogInfoAspect
```

---

# Configuração local

## Porta da aplicação

A aplicação roda localmente na porta:

```text
8082
```

URL base:

```text
http://localhost:8082
```

---

## Banco de dados local

Este serviço utiliza banco próprio, seguindo a estratégia `database per service`.

Banco:

```text
limit_db
```

Host local:

```text
localhost
```

Porta local:

```text
3308
```

Usuário local:

```text
limit_user
```

Senha local:

```text
limit_pass
```

URL JDBC:

```text
jdbc:mysql://localhost:3308/limit_db
```

---

## Docker Compose

Arquivo esperado:

```text
docker-compose.yml
```

Configuração local do MySQL:

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: credit-limit-mysql
    environment:
      MYSQL_DATABASE: limit_db
      MYSQL_USER: limit_user
      MYSQL_PASSWORD: limit_pass
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3308:3306"
```

A porta interna do MySQL continua sendo `3306`, mas na máquina local o acesso é feito pela porta `3308`.

---

## application.yml

Arquivo:

```text
src/main/resources/application.yml
```

Configuração local:

```yaml
spring:
  application:
    name: credit-limit-service

  datasource:
    url: jdbc:mysql://localhost:3308/limit_db
    username: limit_user
    password: limit_pass
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true

server:
  port: 8082

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

---

# Migrations

As migrations ficam em:

```text
src/main/resources/db/migration
```

Padrão de nome:

```text
V1__create_credit_limits_table.sql
V2__create_limit_change_history_table.sql
V3__create_outbox_events_table.sql
V4__create_inbox_events_table.sql
```

Regras:

- usar dois underlines depois da versão;
- nunca alterar uma migration já aplicada;
- criar uma nova migration para cada mudança de banco;
- manter nomes claros e objetivos.

---

## Tabelas planejadas

### credit_limits

Tabela principal de limites de crédito.

Campos principais:

```text
limit_id
customer_id
total_limit
available_limit
used_limit
status
created_at
updated_at
```

---

### limit_change_history

Tabela de histórico de alterações de limite.

Campos principais:

```text
id
limit_id
customer_id
previous_total_limit
new_total_limit
previous_available_limit
new_available_limit
change_type
reason
changed_at
```

---

### outbox_events

Tabela para eventos que serão publicados de forma assíncrona.

Campos principais:

```text
id
event_id
event_type
aggregate_id
payload
status
created_at
published_at
```

---

### inbox_events

Tabela para registrar eventos já consumidos e garantir idempotência.

Campos principais:

```text
id
event_id
event_type
source
processed_at
```

---

# Domínio

## CreditLimit

Representa o limite de crédito de um cliente.

Campos principais:

```text
limitId
customerId
totalLimit
availableLimit
usedLimit
status
createdAt
updatedAt
```

---

## LimitStatus

Status possíveis:

```text
ACTIVE
BLOCKED
CANCELLED
```

---

## Value Objects

### Money

Responsável por representar valores monetários.

Regras:

- deve usar `BigDecimal`;
- não deve aceitar valor negativo;
- deve evitar uso de `Double` ou `Float` para dinheiro.

---

# API / Swagger

A documentação completa da API está disponível via Swagger.

Swagger UI:

```text
http://localhost:8082/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8082/v3/api-docs
```

Os endpoints, contratos de request/response, códigos HTTP e exemplos devem ser consultados diretamente pelo Swagger.

---

# Actuator

A aplicação expõe endpoints operacionais.

## Health

```http
GET /actuator/health
```

Exemplo:

```json
{
  "status": "UP"
}
```

## Metrics

```http
GET /actuator/metrics
```

## Prometheus

```http
GET /actuator/prometheus
```

---

# Como rodar localmente

## 1. Clonar o repositório

```bash
git clone https://github.com/diogobackend/credit-limit-service.git
cd credit-limit-service
```

---

## 2. Subir o MySQL

```bash
docker compose up -d
```

Verificar container:

```bash
docker ps
```

Esperado:

```text
credit-limit-mysql
```

---

## 3. Rodar a aplicação

```bash
./gradlew bootRun
```

A aplicação deve subir em:

```text
http://localhost:8082
```

---

## 4. Validar health

```bash
curl http://localhost:8082/actuator/health
```

Esperado:

```json
{
  "status": "UP"
}
```

---

## 5. Validar Swagger

Acessar no navegador:

```text
http://localhost:8082/swagger-ui.html
```

---

## 6. Acessar o MySQL

```bash
docker exec -it credit-limit-mysql mysql -u limit_user -plimit_pass limit_db
```

Dentro do MySQL:

```sql
SHOW TABLES;
```

---

# Comandos mais usados no dia a dia

## Subir infraestrutura local

```bash
docker compose up -d
```

---

## Parar infraestrutura local

```bash
docker compose down
```

---

## Parar e remover volumes

```bash
docker compose down -v
```

---

## Ver logs dos containers

```bash
docker compose logs -f
```

---

## Ver logs do MySQL

```bash
docker compose logs -f mysql
```

---

## Rodar aplicação

```bash
./gradlew bootRun
```

---

## Rodar build completo

```bash
./gradlew clean build
```

---

## Rodar testes

```bash
./gradlew test
```

---

## Rodar testes com relatório JaCoCo

```bash
./gradlew clean test jacocoTestReport
```

---

## Abrir relatório JaCoCo

```bash
xdg-open build/reports/jacoco/test/html/index.html
```

---

## Rodar ktlint check

```bash
./gradlew ktlintCheck
```

---

## Corrigir formatação com ktlint

```bash
./gradlew ktlintFormat
```

---

## Rodar validação geral antes de commit

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew clean test jacocoTestReport
./gradlew clean build
```

---

## Rodar teste específico

```bash
./gradlew test --tests "*CreateCreditLimitUseCaseTest"
```

Outros exemplos:

```bash
./gradlew test --tests "*FindCreditLimitByCustomerIdUseCaseTest"
./gradlew test --tests "*CalculateInitialLimitUseCaseTest"
./gradlew test --tests "*UpdateCreditLimitUseCaseTest"
./gradlew test --tests "*BlockCreditLimitUseCaseTest"
./gradlew test --tests "*ReleaseCreditLimitUseCaseTest"
```

---

## Limpar build local

```bash
./gradlew clean
```

---

## Ver dependências do projeto

```bash
./gradlew dependencies
```

---

## Validar status do Git

```bash
git status
```

---

## Criar commit

```bash
git add .
git commit -m "feat: setup credit limit service"
```

---

## Enviar alterações

```bash
git push origin master
```

---

# Eventos planejados

Este serviço futuramente publicará eventos de domínio relacionados a limite.

Eventos previstos:

```text
LimitCalculated
LimitUpdated
LimitBlocked
LimitReleased
```

Também poderá consumir:

```text
CustomerEligibilityApproved
```

---

# Logs automáticos

O serviço usará logs automáticos via AOP nos use cases.

Annotations previstas:

```kotlin
@LogInfo
@LogParameter
```

Exemplo:

```kotlin
@LogInfo(logParameters = true, logReturn = true)
fun calculate(@LogParameter input: CalculateInitialLimitInput): CreditLimit
```

Futuramente, essa estrutura será centralizada na lib:

```text
credit-observability-starter
```

---

# Testes

O projeto usará:

- JUnit 5;
- MockK;
- AssertJ;
- JaCoCo.

Testes planejados:

```text
CreateCreditLimitUseCaseTest
FindCreditLimitByCustomerIdUseCaseTest
CalculateInitialLimitUseCaseTest
UpdateCreditLimitUseCaseTest
BlockCreditLimitUseCaseTest
ReleaseCreditLimitUseCaseTest
```

---

# Boas práticas aplicadas

- Arquitetura Hexagonal;
- Separação entre domínio e infraestrutura;
- DTOs apenas nas bordas;
- Entity JPA separada do domínio;
- Mappers explícitos;
- Constructor Injection;
- Flyway para versionamento de banco;
- MySQL isolado para o serviço;
- Actuator para health e métricas;
- Swagger para documentação da API;
- Docker Compose para ambiente local;
- Configuração via `application.yml`;
- Uso de `BigDecimal` para valores monetários;
- JaCoCo para cobertura;
- ktlint para padronização de código.

---

# Próximas evoluções

- Ajustar `build.gradle.kts`;
- Configurar `application.yml`;
- Criar `docker-compose.yml`;
- Criar estrutura hexagonal;
- Criar domínio `CreditLimit`;
- Criar enum `LimitStatus`;
- Criar value object `Money`;
- Criar caso de uso de criação de limite;
- Criar caso de uso de consulta por cliente;
- Criar caso de uso de cálculo inicial;
- Criar migrations;
- Criar adapter REST;
- Criar adapter de persistência;
- Criar testes unitários;
- Publicar eventos de limite futuramente;
- Implementar Outbox Pattern futuramente.

---

# Status

```text
Em desenvolvimento.
Estrutura inicial criada.
```
