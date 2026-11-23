# Meridian API

Spring Boot 3.5 (Java 25) backend for a secondary-school management system: 22 entities, 123 endpoints,
access + refresh token auth, WebSocket events, Redis-backed caching and rate limiting, and the results workflow that ties a
teacher's marks to a published report card.

Built to the same shape as the Palate reference module: one package per entity, one service class per
operation, `record` request DTOs, `@Getter/@Setter` response DTOs, MapStruct mappers, criteria objects for
search, and a `SimpMessagingTemplate` broadcaster per resource.

---

## Quick start

**JDK 25** and Maven 3.9+ (the build targets `release 25`, so older JDKs cannot compile it). Redis is
optional: on startup the app probes for it and uses it when present, otherwise it keeps the cache and the
rate limiter in memory — no configuration needed either way.

```bash
mvn spring-boot:run                      # http://localhost:8080
# or
mvn -B -DskipTests package && java -jar target/meridian-1.0.0.jar
```

What you will see in the log tells you which backend won:

```
RedisAvailability : Redis reachable at localhost:6379 — cache and rate limiting will use it
CacheConfig       : Cache provider: Redis (prefix 'meridian:')
RateLimitConfig   : Rate limiter: Redis (240 requests/min, 60 on auth)
```

…or, with no Redis running:

```
RedisAvailability : No Redis at localhost:6379 (ConnectException) — falling back to in-memory cache and rate limiter
```

| Surface | URL |
| --- | --- |
| Swagger UI | `/swagger-ui.html` |
| OpenAPI JSON | `/v3/api-docs` |
| H2 console | `/h2-console` — `jdbc:h2:mem:meridian`, user `sa`, empty password |
| WebSocket / STOMP | `/ws`, topics `/topic/<resource>/{created,updated,deleted}` |

Seeded accounts — password `Password@123` for all three:

| Role | Identifier | Email |
| --- | --- | --- |
| Admin (ADM/001) | `admin@meridian.test` | Adaeze Obi |
| Teacher (TCH/001) | `teacher@meridian.test` | form teacher of SS2 A |
| Student (STU/2026/001) | `student1@meridian.test` | Amaka Nwosu, SS2 A |

Seed data: session `2026/2027`, terms 1=FIRST (active) / 2=SECOND / 3=THIRD, classes `SS2 A` and `JSS1 A`,
assignments Maths + Physics, grade scale A1 75+, B2 70, B3 65, C4 60, C5 55, C6 50, D7 45, E8 40, F9 <40.

### With Redis

```
docker compose up -d redis        # redis:7.2-alpine, port 6379
mvn spring-boot:run               # auto-detects it
```

That is all — `meridian.cache.provider` and `meridian.ratelimit.backend` default to `auto`, which
means "Redis if the startup probe reaches it". To make Redis mandatory (recommended in production, so a
missing server is loud instead of silent) run with the `redis` profile:

```bash
SPRING_PROFILES_ACTIVE=dev,redis mvn spring-boot:run
```

`application-redis.yml` forces `provider: redis` / `backend: redis` and reads `REDIS_HOST`, `REDIS_PORT`,
`REDIS_PASSWORD`. Either way Redis is **never a hard dependency**: if it goes away mid-flight the cache
degrades to database reads and the limiter fails open — the API keeps serving.

---

## Package layout — one folder per entity

There is no `identity/`, `academic/`, `results/`, `campus/` grouping any more. Every entity owns a
top-level package that holds its whole slice, exactly like `com.app.palate.menu`:

```
com/app/meridian/
├── term/
│   ├── Term.java                 entity (extends BaseEntity)
│   ├── TermController.java       /api/meridian/terms
│   ├── TermRepository.java
│   ├── TermEvents.java           STOMP broadcaster on /topic/terms/*
│   ├── dto/
│   │   ├── TermRequestDTO.java   record
│   │   ├── TermResponseDTO.java  @Getter/@Setter
│   │   └── TermSearchCriteria.java
│   ├── services/                 one class per operation
│   │   ├── CreateTerm.java  GetTermById.java  UpdateTerm.java  DeleteTerm.java
│   │   ├── SearchTerms.java  GetActiveTerm.java
│   ├── utils/TermMapper.java     MapStruct, componentModel = "spring"
│   └── enums/TermName.java
├── common/       BaseEntity, PageResponse
├── config/       security, OpenAPI, CORS, seeder, WebMvcConfig
├── exceptions/   ApiException family + GlobalExceptionHandler
├── security/     JWT filter, principal, SecurityUtils
├── cache/        cache names, properties, memory + redis cache managers
├── ratelimit/    limiter interface, memory + redis implementations, interceptor
└── utils/        ValidationUtils, EntityResolver
```

22 entities: `academicsession announcement attendance auditlog classroom department enrollment feepayment
gradescale leaverequest resultsheet schoolsettings score staff student subject supportticket
teachingassignment term termreport timetableslot user`.

401 Java files, ~15.5k lines, 123 endpoints, 24 top-level packages.

---

## Domain rules the code enforces

- **Marks** — `caScore` ≤ 40 and `examScore` ≤ 60; `total` is always `caScore + examScore`, and the grade
  and remark always come from the `grade_scales` table (`GradeScaleLookup`), never from the client.
- **Result-sheet workflow** — `DRAFT → PENDING → APPROVED | REJECTED`. Only the owning teacher may edit or
  submit; only an admin may approve or reject; an `APPROVED` sheet is immutable (409 on further submits).
- **Visibility** — a student sees a score only when its sheet is `APPROVED` *and* the settings row has
  `allowStudentView`; the full-year view skips terms that are not published (`ResultVisibilityGate`).
- **Ownership scoping** — `/teachers/me/*` and `/students/me/*` resolve the caller; a teacher can only open
  sheets for assignments they own (`CreateResultSheet.requireOwner`).
- **Optional modules** — `schoolsettings.feesEnabled` / `departmentsEnabled`; when off, `ModuleGate` makes
  the route answer **404**, not 403.
- **Timetable** — overlapping slot for the same class or teacher → **409**; day/period validated.
- **Single active term** — activating a term deactivates the others (enforced in `CreateTerm`/`UpdateTerm`).
- **Single settings row** — `GetSchoolSettings` creates it on first read; there is no create/delete route.
- **Audit trail** — `AuditTrail` writes an `audit_logs` row for every mutation, with actor, action and entity.
- **Subject codes are optional** — a junior secondary school that has no codes can post just a name and one
  is generated (`SUB/2026/001`, collision-safe); a code the school does send is kept and stays unique.

---

## API conventions

### Envelope

Every list answers `{ data, page, limit, total, totalPages }` (`PageResponse`). Singles answer the object
directly. Errors are `{ timestamp, status, error, message, path }`.

### Search: filtering, paging, sorting (every list endpoint)

Criteria classes take the same parameter names across all entities, including the aliases the frontend
screens already send:

| Parameter | Alias | Meaning |
| --- | --- | --- |
| `search` | `q` | free-text match over the entity's searchable columns |
| `page` | — | 1-based page number (default 1) |
| `limit` | — | page size, clamped to 1..200 (default 20) |
| `sortBy` | `sort` | whitelisted column (default `id`) |
| `sortDirection` | `order` | `asc` / `desc` (default `desc`) |

Plus per-entity filters and ranges, e.g. `students?currentClassId=1&status=ACTIVE`,
`term-reports?minAverage=60&maxAverage=79`.

Sorting is **validated**: an unknown key is a `400` that lists what is allowed, instead of silently sorting
by `id`. Related fields sort too — `students?sort=fullName&order=asc` orders by the joined `user.fullName`
(the alias `fullName` maps to `user.fullName`), and free text on students matches `studentNo`,
`guardianName`, `user.fullName` and `user.email`.

```bash
curl "$API/students?q=amaka&page=1&limit=10&sort=fullName&order=asc" -H "Authorization: Bearer $TOKEN"
```

### Auth: access + refresh tokens

`POST /auth/login` answers two tokens:

| Token | Kind | Lifetime | How it is checked |
| --- | --- | --- | --- |
| `accessToken` | JWT (HS512, `typ=access`) | 30 min | signature only — stateless |
| `refreshToken` | opaque 256-bit random, stored **hashed** | 14 days | looked up in the token store |

`POST /auth/refresh {"refreshToken"}` returns a **new pair**; the token you sent is spent. Refresh tokens
rotate on every use, so a replayed one is treated as theft: the request is refused **and every session of
that account is revoked**. `POST /auth/logout {"refreshToken"}` revokes that session, `allDevices: true`
revokes all of them.

The store is Redis when it is reachable (`meridian:auth:refresh:*`) and per-JVM memory otherwise, the same
choice the cache and limiter make.

Password reset: `POST /auth/forgot-password` always answers `202` with the same message, so it never
reveals whether an address exists. The token is single use, valid 30 minutes, and mailed by `ResetMailer`
(logged until SMTP is wired); while `meridian.auth.expose-reset-token` is on it is also returned in the
body, which is how an API-only setup can finish the flow. `POST /auth/reset-password` spends it, enforces
`PasswordPolicy` (8+ chars, letter and digit) and **revokes every refresh token** of the account.

### Status codes

`200` ok · `201` created · `400` validation / unknown sort · `401` no or bad token · `403` wrong role
(and bad credentials, deliberately) · `404` unknown id, unknown URL, or a disabled module · `409` conflict
(duplicate sheet, timetable clash, duplicate enrolment) · `429` rate limited.

---

## Caching

Six caches, all in `com.app.meridian.cache.CacheNames`. Only small reference data and derived values are
cached — never result sheets, scores or student lists.

| Cache | TTL | Populated by | Invalidated by |
| --- | --- | --- | --- |
| `schoolSettings` | 30m | `GetSchoolSettings.get()` | any settings write (`update`, `updateModules`, `setLogo`) |
| `activeTerm` | 15m | `GetActiveTerm.get()` | `CreateTerm` / `UpdateTerm` / `DeleteTerm` |
| `gradeScaleByTotal` | 1h | `GradeScaleLookup.forTotal(total)` | grade-scale create/update/delete |
| `announcementFeed` | 2m | `GetAnnouncementFeed.get(role)` | announcement create/update/delete |
| `classTimetable` | 5m | `GetClassTimetable.get(classId, termId)` | slot create/update/delete |
| `termReportsByStudent` | 10m | `LoadStudentReports.load(studentId)` | report compute, remark edit |

- **Providers** — `auto` (default: Redis when the probe reaches it, else Caffeine), or forced `redis` /
  `memory` via `meridian.cache.provider`. Both honour the same cache names and TTLs.
  Redis values are JSON with a `meridian:` key prefix, so `redis-cli keys 'meridian:*'` is readable.
- **Fail-open** — `CacheErrorHandlerConfig` logs and continues; a Redis outage produces slower responses,
  not failed ones.
- **Eviction is wired to the write path**, mostly through `@CacheEvict` on the generated create/update/delete
  services (declared per entity in the codegen spec as `cache_evict=[...]`), so a stale entry cannot outlive
  the mutation that invalidated it. `CacheBehaviourTest` asserts exactly that.
- **Why a separate loader bean** — `GetMyReports` resolves "me" and then calls `LoadStudentReports`; a
  `@Cacheable` method invoked from inside its own class would bypass the proxy and never cache.

## Rate limiting

A fixed-window counter per client, in `com.app.meridian.ratelimit`.

- **Key** — the signed-in account (`user:<id>`), otherwise the caller's IP, honouring `X-Forwarded-For`.
- **Budgets** — `240`/min for ordinary calls, `60`/min for the four credential endpoints
  (`login`, `refresh`, `forgot-password`, `reset-password`) so password guessing
  is expensive. Both configurable, and `meridian.ratelimit.enabled=false` turns the limiter off.
- **Headers** — every API response carries `X-RateLimit-Limit` and `X-RateLimit-Remaining`; a rejection is
  `429` with `Retry-After` seconds and the usual error body.
- **Providers** — `auto` (default), `memory` (per instance) or `redis` (`INCR` + window TTL, shared across
  instances) via `meridian.ratelimit.backend`. If Redis throws, the request is allowed (fail-open) and a
  warning is logged.

```bash
for i in $(seq 1 25); do curl -s -o /dev/null -w '%{http_code} ' \
  -X POST localhost:8080/api/meridian/auth/login \
  -H 'Content-Type: application/json' -d '{"identifier":"x","password":"y"}'; done
# 403 … 429 429 …
```

## Configuration

```yaml
meridian:
  jwt:
    secret: ...            # 64+ chars, HS512
    expiration-minutes: 30            # access token
    refresh-expiration-minutes: 20160 # refresh token (14 days)
  auth:
    expose-reset-token: true          # returns the reset token in the body while there is no SMTP
  cache:
    provider: auto         # auto | redis | memory
    default-ttl: 10m
    key-prefix: "meridian:"
    ttls: { gradeScaleByTotal: 1h, schoolSettings: 30m, activeTerm: 15m,
            announcementFeed: 2m, classTimetable: 5m, termReportsByStudent: 10m }
  ratelimit:
    enabled: true
    requests-per-minute: 240
    auth-requests-per-minute: 60
    window: 1m
    backend: auto          # auto | redis | memory
```

---

## Tests

```bash
mvn -B test        # 17 tests, on JDK 25
```

- `ResultsWorkflowTest` — sheet creation, roster, marks, grade lookup, submit/approve, student visibility,
  module gate, bad credentials (MockMvc, 3 tests).
- `CacheBehaviourTest` — reads through the settings and grade-scale caches, writes through the services, and
  proves the stale entry is gone (2 tests).
- `RateLimitTest` — three bad logins pass, the fourth is `429` with `Retry-After` (2 tests).
- `AuthFlowTest` — both tokens from login, rotation, replayed-token revocation, logout, the whole
  forgot → reset cycle including single use and session revocation (6 tests).
- `SubjectCodeTest` — generated / supplied / blank / non-colliding subject codes (4 tests).

The suite passes with and without a Redis server running: with one, the cache and limiter exercise the
Redis path; without, the same assertions run against the in-memory fallback. `RateLimitTest` clears the
`ratelimit:*` keys first so a leftover counter cannot make it flaky.

The forked test JVM is capped at `-Xmx640m` in the pom; running the suite *and* the packaged app on a small
box at the same time can get the verifier OOM-killed — run them one after the other.

### End-to-end walk

```bash
BASE=http://localhost:8080 ./scripts/smoke-test.sh      # 44 checks, idempotent
```

It signs in three roles, walks the results workflow, checks the 403/404/409 gates, PDF + CSV downloads, the
new list endpoints, paging/sorting/`q` aliases, the `400` on an unknown sort key, and the rate-limit headers.

---

## Toolchain notes (Java 25)

| Item | What it needs | Why |
| --- | --- | --- |
| Spring Boot | **3.5.x or newer** | 3.5 is the first line documented for Java 25 (3.3/3.4 stop at 23/24) |
| `maven.compiler.release` | 25 | class files come out as major version 69 |
| Lombok | **≥ 1.18.40** | earlier versions crash on JDK 25; Boot 3.5.16 manages 1.18.46 |
| MapStruct | 1.6.3 | matches the Lombok/JDK 25 combination |
| Annotation processors | declared in `annotationProcessorPaths` | **since JDK 23 javac ignores processors found on the classpath** — Lombok and MapStruct will not run unless listed, and the build then fails on a thousand missing getters |

`spring-boot-starter-data-redis` came with the same upgrade to Lettuce 6.7 / Spring Data Redis 3.5; the
cache and counter keys are unchanged.

## Code generation

`codegen/spec.py` is the single source of truth (entities, fields, relations, enum ownership, repo methods,
cache invalidations) and `codegen/gen.py` renders the CRUD shell, mappers, DTOs, criteria and search
services. Change the spec, run `python3 gen.py`, then `mvn compile`. Hand-written endpoints (results
workflow, portals, timetable, attendance, fees, leave, tickets, settings) are edited directly.

## Endpoint map

**AcademicSession**

- `GET   ` `/api/meridian/sessions`
- `POST  ` `/api/meridian/sessions`
- `DELETE` `/api/meridian/sessions/{id}`
- `GET   ` `/api/meridian/sessions/{id}`
- `PUT   ` `/api/meridian/sessions/{id}`

**Activity**

- `GET   ` `/api/meridian/activity/recent`

**Announcement**

- `GET   ` `/api/meridian/announcements`
- `POST  ` `/api/meridian/announcements`
- `DELETE` `/api/meridian/announcements/{id}`
- `GET   ` `/api/meridian/announcements/{id}`
- `PUT   ` `/api/meridian/announcements/{id}`

**AnnouncementFeed**

- `GET   ` `/api/meridian/announcements/feed`

**Attendance**

- `GET   ` `/api/meridian/attendance`
- `POST  ` `/api/meridian/attendance`
- `GET   ` `/api/meridian/attendance/summary`

**AuditLog**

- `GET   ` `/api/meridian/audit-logs`

**Auth**

- `POST  ` `/api/meridian/auth/forgot-password`
- `POST  ` `/api/meridian/auth/login`
- `POST  ` `/api/meridian/auth/refresh`
- `POST  ` `/api/meridian/auth/logout`
- `GET   ` `/api/meridian/auth/me`
- `POST  ` `/api/meridian/auth/reset-password`

**ClassRoom**

- `GET   ` `/api/meridian/classes`
- `POST  ` `/api/meridian/classes`
- `DELETE` `/api/meridian/classes/{id}`
- `GET   ` `/api/meridian/classes/{id}`
- `PUT   ` `/api/meridian/classes/{id}`

**Department**

- `GET   ` `/api/meridian/departments`
- `POST  ` `/api/meridian/departments`
- `DELETE` `/api/meridian/departments/{id}`
- `GET   ` `/api/meridian/departments/{id}`
- `PUT   ` `/api/meridian/departments/{id}`

**Enrollment**

- `GET   ` `/api/meridian/enrollments`
- `POST  ` `/api/meridian/enrollments`
- `POST  ` `/api/meridian/enrollments/promote`

**Fee**

- `GET   ` `/api/meridian/fees/payments`
- `POST  ` `/api/meridian/fees/payments`
- `GET   ` `/api/meridian/fees/students/{studentId}`
- `GET   ` `/api/meridian/fees/summary`

**GradeScale**

- `GET   ` `/api/meridian/grading-scale`
- `POST  ` `/api/meridian/grading-scale`
- `DELETE` `/api/meridian/grading-scale/{id}`
- `GET   ` `/api/meridian/grading-scale/{id}`
- `PUT   ` `/api/meridian/grading-scale/{id}`

**Leave**

- `GET   ` `/api/meridian/leave`
- `POST  ` `/api/meridian/leave`
- `PATCH ` `/api/meridian/leave/{id}/decide`
- `GET   ` `/api/meridian/staff/on-leave`

**ResultSheet**

- `GET   ` `/api/meridian/result-sheets`
- `POST  ` `/api/meridian/result-sheets`
- `POST  ` `/api/meridian/result-sheets/bulk-approve`
- `GET   ` `/api/meridian/result-sheets/{id}`
- `PATCH ` `/api/meridian/result-sheets/{id}/approve`
- `GET   ` `/api/meridian/result-sheets/{id}/export`
- `PATCH ` `/api/meridian/result-sheets/{id}/reject`
- `GET   ` `/api/meridian/result-sheets/{id}/scores`
- `POST  ` `/api/meridian/result-sheets/{id}/scores/upload`
- `PUT   ` `/api/meridian/result-sheets/{id}/scores/{studentId}`
- `POST  ` `/api/meridian/result-sheets/{id}/submit`

**SchoolSettings**

- `GET   ` `/api/meridian/settings`
- `PUT   ` `/api/meridian/settings`
- `PUT   ` `/api/meridian/settings/academic`
- `POST  ` `/api/meridian/settings/logo`
- `PUT   ` `/api/meridian/settings/modules`

**Score**

- `GET   ` `/api/meridian/scores`

**Staff**

- `GET   ` `/api/meridian/staff`
- `POST  ` `/api/meridian/staff`
- `DELETE` `/api/meridian/staff/{id}`
- `GET   ` `/api/meridian/staff/{id}`
- `PUT   ` `/api/meridian/staff/{id}`

**Student**

- `GET   ` `/api/meridian/students`
- `POST  ` `/api/meridian/students`
- `DELETE` `/api/meridian/students/{id}`
- `GET   ` `/api/meridian/students/{id}`
- `PUT   ` `/api/meridian/students/{id}`

**StudentPortal**

- `GET   ` `/api/meridian/students/me`
- `GET   ` `/api/meridian/students/me/attendance/summary`
- `GET   ` `/api/meridian/students/me/enrollments`
- `GET   ` `/api/meridian/students/me/fees`
- `GET   ` `/api/meridian/students/me/report-card.pdf`
- `GET   ` `/api/meridian/students/me/reports`
- `GET   ` `/api/meridian/students/me/results`
- `GET   ` `/api/meridian/students/me/results/history`
- `GET   ` `/api/meridian/students/me/results/subject/{subjectId}`
- `GET   ` `/api/meridian/students/{studentId}/results/history`

**Subject**

- `GET   ` `/api/meridian/subjects`
- `POST  ` `/api/meridian/subjects`
- `DELETE` `/api/meridian/subjects/{id}`
- `GET   ` `/api/meridian/subjects/{id}`
- `PUT   ` `/api/meridian/subjects/{id}`

**SupportTicket**

- `GET   ` `/api/meridian/support`
- `POST  ` `/api/meridian/support`
- `DELETE` `/api/meridian/support/{id}`
- `GET   ` `/api/meridian/support/{id}`
- `PUT   ` `/api/meridian/support/{id}`

**TeacherPortal**

- `GET   ` `/api/meridian/teachers/me/result-sheets`
- `GET   ` `/api/meridian/teachers/me/students`
- `GET   ` `/api/meridian/teachers/me/subjects`

**TeachingAssignment**

- `GET   ` `/api/meridian/assignments`
- `POST  ` `/api/meridian/assignments`
- `DELETE` `/api/meridian/assignments/{id}`
- `GET   ` `/api/meridian/assignments/{id}`
- `PUT   ` `/api/meridian/assignments/{id}`

**Term**

- `GET   ` `/api/meridian/terms`
- `POST  ` `/api/meridian/terms`
- `DELETE` `/api/meridian/terms/{id}`
- `GET   ` `/api/meridian/terms/{id}`
- `PUT   ` `/api/meridian/terms/{id}`

**TermReport**

- `GET   ` `/api/meridian/term-reports`
- `PUT   ` `/api/meridian/term-reports/{id}/remark`

**Timetable**

- `GET   ` `/api/meridian/classes/{classId}/timetable`
- `GET   ` `/api/meridian/timetable`
- `POST  ` `/api/meridian/timetable`
- `GET   ` `/api/meridian/timetable/me`
- `GET   ` `/api/meridian/timetable/me/today`
- `GET   ` `/api/meridian/timetable/me/week`
- `DELETE` `/api/meridian/timetable/{id}`
- `PUT   ` `/api/meridian/timetable/{id}`

**User**

- `GET   ` `/api/meridian/users`
- `POST  ` `/api/meridian/users`
- `GET   ` `/api/meridian/users/{id}`
- `PUT   ` `/api/meridian/users/{id}`
- `PATCH ` `/api/meridian/users/{id}/status`
