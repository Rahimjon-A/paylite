# PayLite

PayLite is a small billing application built with Java and Spring Boot.

The application allows agents to create payments using their balance, view their payment history and balance, while administrators can top up agent balances.

## Tech Stack

- Java 17
- Spring Boot
- Maven
- JHipster
- PostgreSQL
- Liquibase
- Keycloak
- Spring Security OAuth2 / OIDC
- HashiCorp Consul
- Spring Cloud Consul
- MapStruct
- Swagger / OpenAPI
- JUnit 5
- Mockito
- Testcontainers
- Docker Compose

## Architecture

Simple layered architecture:

```text
REST Resource
     ↓
   Mapper
     ↓
  Service
     ↓
 Repository
     ↓
 PostgreSQL
```

External services:

```text
Keycloak   → Authentication / Authorization
Consul     → Commission configuration
PostgreSQL → Application data
```

REST DTOs are separated from JPA entities using MapStruct mappers.

## Domain

The application contains exactly two entities.

### Agent

```text
id       Long
login    String
balance  Long
```

### Payment

```text
id                 Long
accountNumber      String
amount             Long
commissionAmount   Long
totalAmount        Long
status             PaymentStatus
createdDate        Instant
agent              Agent
```

Payment statuses:

```text
PAID
FAILED
```

Relationship:

```text
Agent 1 ─────── * Payment
```

## Authentication

Keycloak is used for OAuth2/OIDC authentication.

The application has exactly two application roles:

- `ROLE_AGENT`
- `ROLE_ADMIN`

The authenticated user's `preferred_username` is matched with `Agent.login`.

### Agent permissions

```text
POST /api/payments
GET  /api/payments
GET  /api/agents/me/balance
```

### Admin permissions

```text
POST /api/agents/{id}/topup
```

## API

| Method | Endpoint                 | Role         | Description                  |
| ------ | ------------------------ | ------------ | ---------------------------- |
| POST   | `/api/payments`          | `ROLE_AGENT` | Create payment               |
| GET    | `/api/payments`          | `ROLE_AGENT` | Get current agent's payments |
| GET    | `/api/agents/me/balance` | `ROLE_AGENT` | Get current agent balance    |
| POST   | `/api/agents/{id}/topup` | `ROLE_ADMIN` | Top up agent balance         |

### Create Payment

```http
POST /api/payments
```

```json
{
  "account": "998901234567",
  "amount": 500000
}
```

The application:

1. Gets the current agent from the JWT.
2. Locks the agent row.
3. Calculates the commission.
4. Calculates the total amount.
5. Checks the agent balance.
6. Deducts the total amount if the balance is sufficient.
7. Creates a `PAID` or `FAILED` payment.

Insufficient balance returns:

```http
409 Conflict
```

```json
{
  "code": "INSUFFICIENT_BALANCE",
  "message": "Insufficient agent balance"
}
```

The failed payment is still stored with `FAILED` status and the agent balance remains unchanged.

## Money

All monetary values are stored as `Long` in **tiyin**.

```text
1 so'm = 100 tiyin
```

Example:

```text
100,000 so'm = 10,000,000 tiyin
```

`Long` is used instead of floating-point types to avoid precision problems when storing monetary values.

## Commission

The commission percentage is stored in Consul:

```yaml
application:
  payment:
    commission-percent: 1.5
```

For example:

```text
Amount:       500,000 tiyin
Commission:       1.5%

Commission = 500,000 × 1.5 / 100
           = 7,500 tiyin

Total = 507,500 tiyin
```

Commission calculation uses `BigDecimal` and is rounded to whole tiyin using:

```java
RoundingMode.HALF_UP
```

The commission configuration is refreshable through Spring Cloud Consul without restarting the application.

## Transactions and Concurrency

Payment creation is transactional.

The current agent is loaded using a pessimistic write lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This prevents concurrent payment requests from spending the same balance based on an outdated value.

## Database

PostgreSQL is used as the database.

Liquibase manages database schema changes and initial data.

The development environment contains an initial agent:

```text
login: agent
balance: 10,000,000 tiyin
```

Already executed Liquibase changesets are not modified; new database changes are added through new changesets.

## Docker

Start PostgreSQL, Keycloak and Consul:

```powershell
docker compose -f src/main/docker/services.yml up -d
```

Check containers:

```powershell
docker compose -f src/main/docker/services.yml ps
```

### Local ports

| Service    | Address                 |
| ---------- | ----------------------- |
| PayLite    | `http://localhost:8080` |
| PostgreSQL | `localhost:5433`        |
| Keycloak   | `http://localhost:9080` |
| Consul     | `http://localhost:8500` |

PostgreSQL uses port `5433` on the host to avoid conflicts with an existing PostgreSQL instance on port `5432`.

## Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger is configured with OAuth2 Authorization Code flow and PKCE using the Keycloak `web_app` client.

The documented API is limited to:

```text
/api/**
```

## Running the Application

### Requirements

- Java 17
- Docker Desktop

### Start infrastructure

```powershell
docker compose -f src/main/docker/services.yml up -d
```

### Build

```powershell
.\mvnw.cmd clean compile
```

### Run tests

```powershell
.\mvnw.cmd test
```

### Start application

```powershell
.\mvnw.cmd spring-boot:run
```

## Testing

The project contains tests for the required payment business rules:

- Commission calculation.
- Successful payment and balance deduction.
- Insufficient balance resulting in a `FAILED` payment with unchanged balance.

Mockito is used for unit tests.

Testcontainers is configured for PostgreSQL integration testing.

```powershell
.\mvnw.cmd test
```

## Error Handling

The application uses standard HTTP status codes:

| Status | Meaning                               |
| ------ | ------------------------------------- |
| `200`  | Successful request                    |
| `400`  | Invalid request / validation error    |
| `401`  | Authentication required               |
| `403`  | Insufficient permissions              |
| `404`  | Agent/resource not found              |
| `409`  | Insufficient balance / state conflict |
