1. Project overview

The system will consist of 4 microservices:

                    ┌────────────────────┐
                    │      Keycloak      │
                    │ Authentication     │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │      PayLite       │
                    │                    │
                    │ Agent management   │
                    │ Card binding       │
                    │ Card information   │
                    │ Commission         │
                    │ P2P orchestration  │
                    └──────┬─────┬───────┘
                           │     │
                  ┌────────┘     └────────┐
                  ▼                       ▼
          ┌───────────────┐       ┌───────────────┐
          │   UZCARD      │       │     HUMO      │
          │   Service     │       │    Service    │
          │               │       │               │
          │ Cards         │       │ Cards         │
          │ Balance       │       │ Balance       │
          │ Deposit       │       │ Deposit       │
          │ Withdraw      │       │ Withdraw      │
          │ P2P           │       │ P2P           │
          └───────┬───────┘       └───────┬───────┘
                  │                       │
                  ▼                       ▼
             UZCARD DB                HUMO DB

                    ┌────────────────────┐
                    │   Card Bank        │
                    │     Service        │
                    │                    │
                    │ Customers/Owners   │
                    │ Card information   │
                    │ Card creation      │
                    └─────────┬──────────┘
                              ▼
                         Card Bank DB

Supporting infrastructure:

Keycloak
Consul
PostgreSQL
Liquibase
Docker Compose
OpenFeign
Resilience4j

This is an extension of the PayLite architecture we've already worked with.

Supporting infrastructure:

Keycloak
Consul
PostgreSQL
Liquibase
Docker Compose
OpenFeign
Resilience4j

This is an extension of the PayLite architecture we've already worked with.

2. Service responsibilities
   A. Card Bank Service

Responsible for creating and maintaining card/customer information.

Responsibilities
create card
store card owner
generate PAN
generate expiration date
store card type
provide card information
validate card existence
possibly block/unblock card

It does not own the financial balance.

. UZCARD Service

Responsible exclusively for UZCARD.

Responsibilities
create UZCARD
get card
get balance
deposit money
withdraw money
P2P operation
card operation history
atomic balance updates

UZCARD PAN:

8600 + 12 digits = 16 digits

C. HUMO Service

Same responsibility as UZCARD but for HUMO.

Responsibilities
create HUMO
get card
get balance
deposit money
withdraw money
P2P operation
operation history
atomic balance updates

HUMO PAN:

9860 + 12 digits = 16 digits

D. PayLite Service

This is the orchestration/business layer.

Responsibilities
Agent
bind cards to agents
get card information
calculate commission
initiate P2P
validate P2P
coordinate UZCARD/HUMO
maintain P2P operation
idempotency using requestId
compensation if transfer fails

PayLite should not own card balances.

Entities
Card Bank
CardOwner
CardOwner

---

id
fullName
citizenshipId
phoneNumber

Constraints:

fullName required
citizenshipId required, exactly 14 digits
phoneNumber required

citizenshipId should be String, not Long.

Card
Card

---

id
pan
type
expireDate
pinHash
ownerId
createdAt
status

Potential enum:

CardType {
UZCARD,
HUMO
}

Status:

CardStatus {
ACTIVE,
BLOCKED,
EXPIRED
}

Important

I recommend that balance does not live here.

The financial balance belongs to the corresponding card-network service.

4. UZCARD entities
   Uzcard
   Uzcard

---

id
pan
balance
expireDate
pinHash
ownerId
status
createdAt
updatedAt
UzcardOperation
UzcardOperation

---

id
requestId
cardId
operationType
amount
status
createdAt

Operation:

DEPOSIT
WITHDRAW
P2P
REFUND

5. HUMO entities
   HumoCard
   HumoCard

---

id
pan
balance
expireDate
pinHash
ownerId
status
createdAt
updatedAt
HumoOperation
HumoOperation

---

id
requestId
cardId
operationType
amount
status
createdAt

6. PayLite entities
   Agent
   Agent

---

id
login
createdAt

The login corresponds to the authenticated Keycloak user.

AgentCard

This represents the binding between an Agent and a card.

## AgentCard

id
agentId
pan
type
expireDate
createdAt
status

Relationship:

Agent 1 ───────── \* AgentCard

So:

Agent
├── 8600123456789012 UZCARD
├── 8600987654321098 UZCARD
└── 9860123456789012 HUMO 7. P2P entities
P2POperation
P2POperation

---

id
requestId

amount
commissionAmount
totalAmount

fromPan
fromExpireDate
fromType

toPan
toExpireDate
toType

status
failureReason

createdAt
updatedAt

requestId must be unique.

8. P2P status

I recommend:

P2PStatus {
CREATED,
VALIDATING,
WITHDRAWING,
WITHDRAWN,
TRANSFERRING,
COMPLETED,
FAILED,
COMPENSATING,
COMPENSATED,
COMPENSATION_FAILED
}

This gives us enough information to understand what happened to a payment.

9. Main API requirements
   Card Bank
   Create card
   POST /api/cards

Request:

{
"fullName": "John Doe",
"citizenshipId": "12345678901234",
"phoneNumber": "+998901234567",
"type": "UZCARD"
}

Response:

{
"pan": "8600123456789012",
"type": "UZCARD",
"expireDate": "2029-10-02",
"owner": {
"fullName": "John Doe",
"citizenshipId": "12345678901234",
"phoneNumber": "+998901234567"
}
}

PIN should not be returned.

Get card
GET /api/cards/{pan} 10. UZCARD APIs
POST /api/cards
GET /api/cards/{pan}
GET /api/cards/{pan}/balance
POST /api/cards/{pan}/deposit
POST /api/cards/{pan}/withdraw
POST /api/p2p 11. HUMO APIs

Same contract:

POST /api/cards
GET /api/cards/{pan}
GET /api/cards/{pan}/balance
POST /api/cards/{pan}/deposit
POST /api/cards/{pan}/withdraw
POST /api/p2p 12. PayLite APIs
Bind card
POST /api/cards/bind
{
"pan": "8600123456789012",
"expireDate": "2029-10-02",
"type": "UZCARD"
}

The Agent comes from the authenticated Keycloak identity.

Get card
GET /api/cards/{pan}

PayLite determines which service owns the card and requests its information.

Get commission
POST /api/commission
{
"fromType": "UZCARD",
"toType": "HUMO",
"amount": 100000
}

Response:

{
"amount": 100000,
"commissionAmount": 1000,
"totalAmount": 101000
}

The exact commission calculation still needs your confirmation.

13. P2P API
    POST /api/p2p

Request:

{
"requestId": "550e8400-e29b-41d4-a716-446655440000",
"amount": 100000,
"from": {
"pan": "8600123456789012",
"expireDate": "2029-10-02",
"type": "UZCARD"
},
"to": {
"pan": "9860123456789012",
"expireDate": "2029-11-20",
"type": "HUMO"
}
} 14. P2P requirements

The operation must:

Step 1

Validate request.

amount > 0
requestId != null
from != null
to != null
Step 2

Check duplicate requestId.

If already processed:

return previous result

Do not execute payment again.

Step 3

Validate sender.

exists
ACTIVE
not expired
correct type
Step 4

Validate recipient.

Same checks.

Step 5

Check:

sender balance >= required amount
Step 6

Calculate commission.

Step 7

Withdraw sender money.

Step 8

Deposit into recipient.

Step 9

If successful:

P2P = COMPLETED
Step 10

If withdrawal succeeds but deposit fails:

COMPENSATING
↓
refund sender
↓
COMPENSATED

If refund itself fails:

COMPENSATION_FAILED

That case must be persisted for reconciliation.

15. Business invariants

These should be hard requirements.

Card
PAN = exactly 16 digits
UZCARD starts with 8600
HUMO starts with 9860
PAN unique
balance >= 0
new balance = 0
expiration = creation date + 3 years
citizenshipId = exactly 14 digits
P2P
amount > 0
sender != recipient
requestId unique
expired card cannot participate
blocked card cannot participate
insufficient balance → no withdrawal
successful transfer:
sender -= amount/required amount
recipient += amount
failed transfer after withdrawal:
sender gets refunded 16. Questions I need you to answer

Now these are the actual things I need from you before I write the final requirements, because they cannot safely be guessed.

A. Card creation

1. Who creates a card?

Do you want:

Client
↓
Card Bank
↓
UZCARD/HUMO

or:

Client
↓
UZCARD/HUMO
↓
Card Bank
B. Card ownership

2. Should Card Bank and UZCARD/HUMO contain the same card record?

I recommend:

Card Bank
→ owner/card metadata

UZCARD/HUMO
→ financial card/account

Is that what you want?

C. Balance

3. Where should the balance live?

I strongly recommend:

UZCARD balance → UZCARD DB
HUMO balance → HUMO DB

and not Card Bank.

Confirm this.

D. PIN

4. Is 5555 really required as the default PIN?

And should users be allowed to change their PIN later?

YES / NO
E. Commission — very important

5. Who pays commission?

Choose one:

A

Sender has 100,000
Commission = 1,000

Sender loses 101,000
Recipient receives 100,000

B

Sender has 100,000
Commission = 1,000

Sender loses 100,000
Recipient receives 99,000

C

Sender loses 100,000
Recipient receives 100,000
Commission is handled separately

Or tell me your intended rule.

F. Commission rates

6. What are the commission percentages?

For:

UZCARD → UZCARD
UZCARD → HUMO
HUMO → UZCARD
HUMO → HUMO

If you don't have numbers yet, we can define them as configuration placeholders:

commission:
uzcard-to-uzcard: 0
uzcard-to-humo: 1
humo-to-uzcard: 1
humo-to-humo: 0

and change them later.

G. P2P ownership

7. Do you want P2P to exist inside UZCARD/HUMO services AND PayLite?

Your description suggests:

UZCARD service → its own P2P
HUMO service → its own P2P
PayLite → cross-network P2P

Is that correct?

For example:

UZCARD → UZCARD

can be handled by UZCARD directly, while:

UZCARD → HUMO

is orchestrated by PayLite.

Or do you want all P2P operations to go through PayLite?

H. Card binding

8. Can one card be bound to multiple agents?

I recommend:

one card → one agent
one agent → many cards

Is that correct?

I. Card creation and agent

9. Does creating a card automatically bind it to the current Agent?

Or:

create card
↓
card exists

later:

bind card
↓
AgentCard

Your description sounds like the second one, but I want to confirm.

J. Authentication

10. Should Card Bank / UZCARD / HUMO also be protected by Keycloak, or only PayLite?

Possible architecture:

External client
↓
Keycloak
↓
PayLite
↓
internal services

with service-to-service authentication internally.

Or every service independently validates JWT.

K. Card information

11. What should get card info return?

I recommend:

{
"pan": "8600...",
"type": "UZCARD",
"expireDate": "2029-10-02",
"balance": 100000,
"owner": {
"fullName": "...",
"phoneNumber": "..."
},
"status": "ACTIVE"
}

but never:

PIN
pinHash
citizenshipId

unless there is a specific administrative requirement.

L. Money units

12. Confirm we continue using PayLite's existing rule:

Long

- amount in TIYIN

So:

1 so'm = 100 tiyin

and:

40,000 so'm = 4,000,000

That is the approach we previously used in PayLite.

M. Card number validation

13. Do you want Luhn validation?

For a realistic simulated card system:

8600xxxxxxxxxxxx

should not merely have the right prefix/length; it can also satisfy the Luhn checksum.

YES / NO
N. Card number generation

14. Should PAN generation happen in Card Bank or in UZCARD/HUMO?

My recommendation:

Card Bank
↓
requests card creation
↓
UZCARD generates 8600...
HUMO generates 9860...

because the card-network service should own its own PAN namespace.

O. P2P amount

15. Is the P2P amount the amount the recipient receives?

For example:

amount = 100,000

Does recipient always receive:

100,000

and commission is added/removed separately?

This is connected to question E.

You can answer simply like:

1. Card Bank creates
2. Yes
3. UZCARD/HUMO DB
4. Yes, fixed 5555 for now
5. A
6. UZ-UZ 0%, UZ-H 1%, H-UZ 1%, H-H 0%
7. All P2P through PayLite
8. One card -> one agent
9. Separate bind
10. ...
    ...
