# Test Strategy Notes

## Approach

Before writing any tests, I read the actual source across all three services
(claims-service, bff-service, demo-app-ui) rather than testing from the outside
in. No tests existed in the repo beforehand. The goal was to find the highest-risk
areas by reading the code, then spend the available time there rather than
spreading effort evenly.

## Architecture, briefly

Three services: a Next.js UI, a Spring Boot BFF (auth, Keycloak, WebSocket
fan-out), and a Spring Boot claims-service (the actual domain/business rules).
Claims can reach Kafka's `claim-events` topic by two different paths depending
on a single boolean (`app.events.cdc-enabled`): published directly by the app
after each write, or captured from Postgres by a separate Debezium CDC
connector. The BFF consumes both that topic and Debezium's raw CDC topic,
normalizes both shapes, and pushes updates to browsers over WebSocket.

That dual-path event design is the single most structurally risky piece of the
system — not because either path is badly written, but because it doubles the
number of ways the "claim changed" signal can reach the UI, and the flag
controlling which path is live is easy to get out of sync with what's actually
running. This was confirmed in practice, not just in theory — see "Issues
discovered" below.

## Risk assessment (ranked)

| Area | Why it's high-risk | Priority |
|---|---|---|
| Claim status transitions (`Claim.updateStatus` / `ClaimStatus`) | The entire admin workflow depends on one switch statement, duplicated by hand in the frontend (`claim-status-transitions.ts`). | Highest |
| Claim creation validation (`Claim` constructor) | Only place amount/date/length rules are enforced. Every entry point funnels through it. | Highest |
| RBAC on admin status updates (`UpdateClaimStatusUseCase`) | Checks role in the use case itself, not just via `@PreAuthorize` at the controller — worth testing independently. | High |
| CDC-enabled toggle & dual event-publish paths | One flag changes which of two very different code paths runs, with no compile-time signal if it's wrong. Already caused a real incident (see below). | High |
| Ownership checks on claim read access (`GetClaimUseCase`) | Hard denies any request where `requestingUserId != claim.userId`, no admin bypass. Currently masked by a frontend bug (see below) but a landmine for later. | Medium |
| Kafka message schema validation / envelope building | Manual if/instanceof chains per event type; adding a type without updating all three chains fails at runtime, not compile time. | Medium |
| Currency as `double` at the API boundary | `CreateClaimRequestDTO.getClaimAmount()` is a primitive `double`, converted via `BigDecimal.valueOf(double)`. Wrong type for money, low practical risk given 2-decimal UI input. | Low-Medium |
| User provisioning idempotency (`UserController.createUser`) | Handles true-idempotent replay and a Keycloak-ID-changed case with a raw SQL update — correct-looking, untested edge case. | Low-Medium |

## What was tested, and why

- **`ClaimTest.java`** — full validation boundary coverage: future dates,
  description/location length boundaries (including a whitespace-padding edge
  case), amount boundaries (zero, negative, exactly at max, one cent over).
- **`ClaimStatusTest.java`** — the full 5x5 transition matrix (25 cases) as a
  parameterized test, not spot checks, since this table is the actual spec of
  the admin workflow.
- **`CreateClaimUseCaseTest.java`** — unknown-user rejection, and both branches
  of the CDC-enabled toggle (direct publish vs. no-op) — the exact flag that
  already caused a real incident.
- **`UpdateClaimStatusUseCaseTest.java`** — RBAC (non-admin rejected, unknown
  user rejected), not-found, illegal transition, and a test that pins down the
  `changedBy` attribution bug precisely (see "Issues discovered").

Result: **55 tests, 0 failures**, run via `mvn -f code/claims-service/pom.xml test`
against the real codebase.

## What was deliberately left out, and why

- **Controller / HTTP layer (`@WebMvcTest`)** — controllers are thin pass-throughs
  to the use cases already covered; the MockMvc setup cost outweighed the
  additional risk coverage.
- **Full integration tests with Testcontainers** (Postgres/Kafka/Keycloak) —
  high value, recommended as the next investment, not skipped for low priority.
  Just outside the time budget for this pass.
- **bff-service** (auth, WebSocket fan-out, Kafka consumer) — reviewed, one
  design note included below, but not unit tested; claims-service owns all the
  business rules, bff-service is mostly a thin adapter/fan-out layer.
- **demo-app-ui component tests** — the existing Playwright E2E suite already
  exercises the main flow end-to-end; lower priority than closing two real
  backend bugs.
- **Currency-as-double precision** — flagged as a risk, not written up as a
  full test since it's a code-quality concern rather than a demonstrated
  failure. Worth a follow-up boundary test if pursued further.

## Issues discovered

### Bug 1 — Admin "View Details" button is a non-functional stub

`demo-app-ui/src/features/admin/components/admin-claims-table.tsx`:

```ts
const handleViewDetails = () => {
  toast('Details view coming soon');
};
```

Confirmed live during manual testing — clicking it only shows a toast, never
opens a modal or calls an API. Related design risk for whenever this gets
built: the claimant-side `ClaimDetailModal` calls `claimsApi.getClaim()`,
backed by `GetClaimUseCase`, which hard-rejects any request where the
requester isn't the claim's owner — **no ADMIN bypass**. If "View Details" is
wired up by reusing that same call, it will 403 for every claim except ones
the admin happens to own themselves.

### Bug 2 — Status-change events are attributed to the wrong user

`claims-service/src/main/java/com/example/demo/domain/claim/Claim.java`,
inside `updateStatus()`:

```java
this.domainEvents.add(new ClaimStatusChanged(
    UUID.randomUUID().toString(),
    this.claimId,
    oldStatus,
    newStatus,
    this.userId,   // <-- the CLAIM OWNER's id, not the admin performing the change
    Instant.now()
));
```

`ClaimStatusChanged.changedBy` is documented as "who changed the status," but
`updateStatus()` only ever receives the new status, never the acting admin's
id — so it always fills `changedBy` with the claim owner's own userId.
`UpdateClaimStatusUseCase` does have the real admin id available
(`command.adminUserId()`) but never passes it through.

Confirmed independently during manual testing, before this code review: a
Kafka message captured live from `claim-events` after an admin rejected a
claim showed `"userId":"09aab177-..."`, which matched the **claimant's** own
user ID, not the admin's. Anyone consuming this event stream for an audit
log, notifications, or compliance reporting cannot tell which admin
approved/rejected a claim.

**Fix direction:** thread the acting admin's UUID through
`UpdateClaimStatusCommand -> Claim.updateStatus(ClaimStatus newStatus, UUID changedBy)`
and use that parameter instead of `this.userId`.

### Infrastructure/tooling issues found during manual setup (not app-code bugs, but real and reproducible)

- `podman compose -f docker-compose.apps.yml --in-pod false up --build -d`
  rebuilds and re-tags Docker images but does **not** recreate already-running
  containers — they stay on the old image. Confirmed via unchanged Container
  IDs after a rebuild. Workaround: explicit `stop` + `rm` in dependent-first
  order (`ui` → `bff-service` → `claims-service`) before `up`.
- `docker-compose.cdc.yml` (the Debezium CDC stack) is never referenced in the
  setup guide, so it's never started by default — the `cdc.demo-app.public.claims`
  Kafka topic silently stays empty. Once started, the connector's task fails
  immediately with `Publication autocreation is disabled` — `publication.autocreate.mode=disabled`
  requires a manual `CREATE PUBLICATION dbz_publication FOR TABLE public.claims;`
  as the Postgres superuser, which nothing automates today.

## Running the suite

```
cd demo-app
mvn -f code/claims-service/pom.xml test
```

Requires no running containers — all four test files are pure unit tests
(domain logic + Mockito-mocked use cases), so they run in well under a second
of actual test time once Maven's warm.
