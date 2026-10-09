# PayLite

PayLite is a Java and Spring Boot payment service that supports agent billing and card-to-card (P2P) transfers between UZCARD and HUMO cards.

The service provides payment processing, commission calculation, balance management, transfer status tracking, and compensation handling for failed P2P operations.

**Related project:** [PayLite P2P Microservices](https://github.com/Rahimjon-A/paylite-microservices)

## Tech Stack

- Java 17
- Spring Boot
- Maven
- JHipster
- PostgreSQL
- Liquibase
- Spring Data JPA / Hibernate
- Spring Security OAuth2 / OIDC
- Keycloak
- HashiCorp Consul
- Spring Cloud Consul
- Spring Cloud OpenFeign
- MapStruct
- Swagger / OpenAPI
- JUnit 5
- Mockito
- Testcontainers
- Docker Compose

## Architecture

PayLite uses a layered architecture with REST controllers, services, repositories, DTOs, and MapStruct mappers.

```text
                         ┌────────────────────┐
                         │ Keycloak / :9080   │
                         │ OAuth2 / OIDC      │
                         │ JWT Authentication│
                         └─────────┬──────────┘
                                   │
                              JWT Access Token
                                   │
                                   ▼
┌──────────────┐        ┌────────────────────┐
│ React        │───────▶│ PayLite / :8080     │
│ Frontend     │  REST  │                    │
│ Postman      │  + JWT │ Billing + P2P API  │
└──────────────┘        └─────────┬──────────┘
                                  │
                  ┌───────────────┼────────────────┐
                  │               │                │
                  ▼               ▼                ▼
          ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
          │ Consul       │ │ PostgreSQL   │ │ Card Bank    │
          │ :8500        │ │ :5433        │ │ :8081        │
          │ Configuration│ │ PayLite data │ │ Card routing │
          └──────────────┘ └──────────────┘ └──────┬───────┘
                                                   │
                                       ┌───────────┴───────────┐
                                       ▼                       ▼
                               ┌──────────────┐       ┌──────────────┐
                               │ UZCARD       │       │ HUMO         │
                               │ :8082        │       │ :8083        │
                               └──────────────┘       └──────────────┘
```

### Main Responsibilities

| Component  | Responsibility                                                                                |
| ---------- | --------------------------------------------------------------------------------------------- |
| PayLite    | Billing payments, P2P transfers, commission calculation, operation tracking, and compensation |
| Card Bank  | Card metadata and routing operations to the appropriate network                               |
| UZCARD     | UZCARD account operations and balance management                                              |
| HUMO       | HUMO account operations and balance management                                                |
| Keycloak   | Authentication and authorization                                                              |
| Consul     | Centralized configuration and service discovery                                               |
| PostgreSQL | Persistent application data                                                                   |

The card-related services are maintained in the [microservices repository](https://github.com/Rahimjon-A/paylite-microservices).

## Features

### Agent Billing

- Create payments using an agent's balance.
- View the authenticated agent's payment history.
- Retrieve the current agent's balance.
- Allow administrators to top up agent balances.
- Calculate commissions using centralized configuration.
- Reject payments when the agent has insufficient funds.

### P2P Card Transfers

- Transfer money between UZCARD and HUMO cards.
- Retrieve card information through Card Bank.
- Preview transfer commissions before submitting a transfer.
- Calculate the total amount charged to the sender.
- Validate sender balance before processing a transfer.
- Withdraw funds from the sender's card network.
- Deposit the transfer amount into the recipient's card network.
- Track transfer progress and final operation status.
- Use compensation handling when a later transfer step fails.
- Identify operations using a unique request ID.

## API

All endpoints are relative to:

```text
http://localhost:8080
```

Authenticated endpoints require an appropriate Keycloak access token unless configured otherwise.

### Billing Endpoints

| Method | Endpoint                 | Role         | Description                           |
| ------ | ------------------------ | ------------ | ------------------------------------- |
| POST   | `/api/payments`          | `ROLE_AGENT` | Create a billing payment              |
| GET    | `/api/payments`          | `ROLE_AGENT` | Retrieve the current agent's payments |
| GET    | `/api/agents/me/balance` | `ROLE_AGENT` | Retrieve the current agent's balance  |
| POST   | `/api/agents/{id}/topup` | `ROLE_ADMIN` | Top up an agent's balance             |

### P2P Endpoints

| Method | Endpoint                      | Description                                                                       |
| ------ | ----------------------------- | --------------------------------------------------------------------------------- |
| POST   | `/api/p2p`                    | Execute a card-to-card transfer                                                   |
| POST   | `/api/p2p/commission-preview` | Calculate the commission and total transfer amount without executing the transfer |

### Create a Billing Payment

```http
POST /api/payments
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN
```

Request:

```json
{
  "account": "998901234567",
  "amount": 500000
}
```

The application identifies the agent from the authenticated JWT, calculates the commission, checks the available balance, and processes the payment.

If the balance is insufficient, the application returns `409 Conflict` and records the failed payment with `FAILED` status. The agent's balance remains unchanged.

### Execute a P2P Transfer

```http
POST /api/p2p
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN
```

Request:

```json
{
  "requestId": "unique-request-id",
  "amount": 100000,
  "fromPan": "8600000000000000",
  "toPan": "9860000000000000"
}
```

The card numbers are illustrative. Use valid cards created in your environment.

The `amount` is expressed in tiyin.

### Preview a P2P Commission

```http
POST /api/p2p/commission-preview
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN
```

This endpoint calculates the applicable commission, commission amount, and total transfer amount for the proposed transfer.

The preview does not create a transfer operation or move money.

Use the request DTO defined by the current P2P controller for the exact preview request fields.

## P2P Transfer Workflow

The P2P transfer is coordinated by PayLite.

1. Receive the transfer request and its unique request ID.
2. Retrieve sender and recipient card information through Card Bank.
3. Determine the card networks and applicable commission.
4. Calculate the commission and total sender charge.
5. Validate the sender's available balance.
6. Withdraw the total charge from the sender's card account.
7. Deposit the transfer amount into the recipient's card account.
8. Record the final operation status and return the result.

If the recipient-side deposit fails after the sender has been charged, PayLite attempts to compensate by reversing the sender-side withdrawal.

The operation status records processing progress and the result of compensation.

### P2P Operation Statuses

| Status                | Meaning                          |
| --------------------- | -------------------------------- |
| `CREATED`             | Operation created                |
| `VALIDATING`          | Validating the transfer          |
| `BALANCE_CHECKED`     | Balance validation completed     |
| `WITHDRAWN`           | Sender withdrawal completed      |
| `PAYING`              | Processing the recipient payment |
| `COMPLETED`           | Transfer completed               |
| `COMPENSATING`        | Compensation is in progress      |
| `COMPENSATED`         | Compensation completed           |
| `FAILED`              | Operation failed                 |
| `COMPENSATION_FAILED` | Compensation failed              |

These statuses describe the P2P operation lifecycle and are distinct from the billing payment statuses.

## Commission Configuration

PayLite reads commission settings from Consul.

Consul's Key/Value configuration path:

```text
config/paylite-service/data
```

Current configuration:

```yaml
paylite:
  commission:
    uzcard-to-uzcard: 0
    uzcard-to-humo: 1
    humo-to-uzcard: 1
    humo-to-humo: 2

application:
  payment:
    commission-percent: 1.5
```

### P2P Commission Rules

| Sender | Recipient | Commission |
| ------ | --------- | ---------: |
| UZCARD | UZCARD    |         0% |
| UZCARD | HUMO      |         1% |
| HUMO   | UZCARD    |         1% |
| HUMO   | HUMO      |         2% |

The network-specific rules apply to P2P transfers. The `application.payment.commission-percent` property configures the billing payment commission.

### Billing Commission Example

For a billing payment of `500,000` tiyin with a commission of `1.5%`:

```text
Amount:       500,000 tiyin
Commission:     7,500 tiyin
Total:        507,500 tiyin
```

Commission calculations use `BigDecimal` and `RoundingMode.HALF_UP` to avoid floating-point precision problems.

The commission configuration can be refreshed through Spring Cloud Consul when the relevant refresh configuration is enabled.

## Money Representation

All monetary amounts are represented as `Long` values in tiyin.

```text
1 so'm = 100 tiyin
```

For example:

```text
100,000 so'm = 10,000,000 tiyin
```

Using integer monetary units avoids floating-point precision errors.

## Transactions and Failure Handling

### Billing Payments

Billing payment creation is transactional. The agent's database record is loaded with a pessimistic write lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This prevents concurrent payment requests from spending the same balance based on an outdated value.

### P2P Transfers

P2P processing involves multiple services and databases, so a single local database transaction cannot make the entire transfer atomic.

PayLite tracks operation progress and uses compensation to handle certain failures after funds have been withdrawn.

The P2P request ID is protected by a database uniqueness constraint to prevent duplicate operation claims.

Compensation is a recovery mechanism, not a guarantee that every distributed failure will be reversed successfully. The `COMPENSATION_FAILED` status records cases requiring further recovery.

## Authentication and Authorization

Keycloak provides OAuth2/OIDC authentication. Spring Security validates JWT access tokens and applies role-based authorization.

The application roles are:

- `ROLE_AGENT`
- `ROLE_ADMIN`

The authenticated user's `preferred_username` is matched with `Agent.login`.

For local development, Keycloak is available at:

```text
http://localhost:9080
```

The realm used by the project is `paylite`.

## Database

PostgreSQL stores PayLite's persistent data.

Liquibase manages schema changes and initial data.

The development environment contains an initial agent:

```text
login: agent
balance: 10,000,000 tiyin
```

Already executed Liquibase changesets should not be modified. Add new schema changes through new changesets.

## Docker

Start PayLite's local infrastructure:

```powershell
docker compose -f src/main/docker/services.yml up -d
```

Check container status:

```powershell
docker compose -f src/main/docker/services.yml ps
```

### Local Ports

| Component   | Address                 |
| ----------- | ----------------------- |
| PayLite API | `http://localhost:8080` |
| PostgreSQL  | `localhost:5433`        |
| Keycloak    | `http://localhost:9080` |
| Consul      | `http://localhost:8500` |
| Card Bank   | `http://localhost:8081` |
| UZCARD      | `http://localhost:8082` |
| HUMO        | `http://localhost:8083` |
| Frontend    | `http://localhost:5173` |

The additional services must be started using the Compose configuration and startup instructions in the microservices repository.

PostgreSQL uses host port `5433` to avoid conflicting with a locally installed PostgreSQL instance on port `5432`.

## Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger uses the configured Keycloak OAuth2 Authorization Code flow with PKCE.

The documented API is limited to:

```text
/api/**
```

## Running the Application

### Requirements

- JDK 17
- Docker Desktop
- Maven Wrapper included in the project

### 1. Start Infrastructure

```powershell
docker compose -f src/main/docker/services.yml up -d
```

### 2. Build

```powershell
.\mvnw.cmd clean compile
```

### 3. Run Tests

```powershell
.\mvnw.cmd test
```

### 4. Start PayLite

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

For P2P transfers, ensure Card Bank, UZCARD, HUMO, Keycloak, Consul, and PostgreSQL are also running and configured correctly.

## Testing

The project includes tests for business rules such as:

- Commission calculation.
- Successful billing payments and balance deduction.
- Insufficient billing balance with unchanged balance.
- P2P commission preview and transfer processing, where covered by the current test suite.
- P2P failure and compensation behavior, where covered by the current test suite.

Mockito is used for unit testing, and Testcontainers is configured for PostgreSQL integration testing.

Run the test suite:

```powershell
.\mvnw.cmd test
```

## Error Handling

Common HTTP status codes include:

| Status | Meaning                                |
| ------ | -------------------------------------- |
| `200`  | Request completed successfully         |
| `400`  | Invalid request or validation error    |
| `401`  | Authentication required                |
| `403`  | Insufficient permissions               |
| `404`  | Agent or resource not found            |
| `409`  | Insufficient balance or state conflict |

The actual response status depends on the endpoint and the error-handling configuration.

## Related Documentation

- [PayLite P2P Microservices](https://github.com/Rahimjon-A/paylite-microservices)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Consul Documentation](https://developer.hashicorp.com/consul/docs)
