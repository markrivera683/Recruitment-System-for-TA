# System Design

**Project:** TA Recruitment System v1.0.0  
**Architecture style:** Layered monolith (Servlet + JSP + Service + FileStore)

---

## 1. Overview

The application is a Java WAR deployed on Tomcat. HTTP requests flow through servlets, which delegate to services that read and write **JSON text files** under `WEB-INF/data/`. CV files remain on the filesystem under `WEB-INF/data/cv/{userId}/`. No database is used (coursework compliance).

```
Browser (JSP views)
    ↓ HTTP
Servlet layer (com.bupt.ta.servlet)
    ↓
Service layer (com.bupt.ta.service, com.bupt.ta.service.ai)
    ↓
FileStore / JobService JSON I/O
    ↓
WEB-INF/data/*.json (+ CV filesystem)
```

Cross-cutting: `AppConfig` (configuration), `PasswordHasher` (BCrypt), `AuditService`, `NotificationService` (SMTP), AI layer (`com.bupt.ta.ai`).

---

## 2. Package structure

```mermaid
graph TB
    subgraph presentation["Presentation"]
        JSP["WEB-INF/jsp/*.jsp"]
        Static["static/css, static/js"]
    end

    subgraph servlet["com.bupt.ta.servlet"]
        BaseServlet
        LoginServlet
        JobServlet
        MoServlet
        AdminServlet
        AiStreamServlet
    end

    subgraph service["com.bupt.ta.service"]
        AuthService
        ProfileService
        JobService
        ApplicationService
        FileStore
        subgraph ai_svc["com.bupt.ta.service.ai"]
            AiFeatureService
            RecommendationService
            MissingSkillService
        end
    end

    subgraph persistence["com.bupt.ta.persistence"]
        ServiceFactory
        AppInitListener
    end

    subgraph ai["com.bupt.ta.ai"]
        LmClientFactory
        MockLmClient
        HttpLmClient
    end

    subgraph files["WEB-INF/data"]
        usersJson[users.json]
        jobsJson[jobs.json]
        appsJson[applications.json]
        cvDir[cv/userId/]
    end

    JSP --> servlet
    servlet --> service
    service --> FileStore
    FileStore --> files
    JobService --> jobsJson
    ProfileService --> cvDir
    persistence --> service
    AiStreamServlet --> ai_svc
    ai_svc --> ai
```

| Package | Responsibility |
|---------|----------------|
| `servlet` | HTTP routing, session auth, request validation, JSP forwarding |
| `service` | Business rules, orchestration, JSON file persistence |
| `persistence` | `ServiceFactory` wiring, startup listener |
| `model` | Domain POJOs (`User`, `Job`, `Application`, …) |
| `ai` | LM client abstraction (mock + HTTP) |
| `security` | Password hashing, CSRF filter |
| `util` | Config resolution, validation helpers |

Startup sequence: `AppInitListener` → `ServiceFactory.fromServletContext` → services stored in `ServletContext`.

---

## 3. Data model (JSON files)

```mermaid
erDiagram
    users ||--o| profiles : "user_id PK/FK"
    users ||--o{ applications : "user_id"
    users ||--o{ favorites : "user_id"
    users ||--o{ recently_viewed : "user_id"
    users ||--o{ password_reset_tokens : "user_id"
    users ||--o{ audit_logs : "actor_id optional"
    jobs ||--o{ favorites : "job_id"
    jobs ||--o{ recently_viewed : "job_id"
    jobs }o--|| users : "created_by_mo_id"

    users {
        varchar id PK
        varchar name
        varchar student_id
        varchar email UK
        varchar password_hash
        varchar role
        boolean active
    }

    profiles {
        varchar user_id PK_FK
        varchar full_name
        varchar student_id
        varchar id_card
        text skills
        varchar cv_file_name
        text education_json
    }

    jobs {
        varchar id PK
        varchar module_name
        varchar module_code
        text required_skills
        varchar status
        varchar created_by_mo_id FK
        varchar application_deadline
    }

    applications {
        varchar id PK
        varchar user_id FK
        varchar module_code
        varchar status
        text feedback
    }

    favorites {
        varchar user_id PK_FK
        varchar job_id PK_FK
    }

    recently_viewed {
        varchar user_id PK_FK
        varchar job_id PK_FK
        varchar viewed_at PK
    }

    audit_logs {
        varchar id PK
        varchar actor_id
        varchar action
        varchar target_type
        varchar created_at
    }

    password_reset_tokens {
        varchar token PK
        varchar user_id FK
        varchar expires_at
    }
```

Indexes: `applications(user_id)`, `applications(status)`, `jobs(status)`, `audit_logs(created_at)`.

---

## 4. AI feature flow (SSE)

TA and MO AI features stream via Server-Sent Events at `/api/ai/stream`. Servlets do not call the LM client directly; feature services build prompts, `AiFeatureService` invokes `LmClient`.

```mermaid
sequenceDiagram
    participant Browser
    participant AiStreamServlet
    participant FeatureSvc as RecommendationService / MissingSkillService / WorkloadAdviceService
    participant AiFeature as AiFeatureService
    participant Factory as LmClientFactory
    participant LM as MockLmClient or HttpLmClient

    Browser->>AiStreamServlet: GET /api/ai/stream?feature=...&jobId=...
    AiStreamServlet->>AiStreamServlet: Verify session + role (TA or MO)
    AiStreamServlet->>FeatureSvc: Build prompt from profile + job data
    FeatureSvc->>AiFeature: execute(LmRequest)
    AiFeature->>Factory: createClient(LmConfig)
    Factory->>LM: resolve provider (mock if no API key)
    AiFeature->>LM: streamCompletion(request, listener)
    loop SSE deltas
        LM-->>AiFeature: token delta
        AiFeature-->>AiStreamServlet: LmStreamListener.onDelta
        AiStreamServlet-->>Browser: data: {"type":"delta","b64":"..."}
    end
    AiStreamServlet-->>Browser: data: {"type":"done"}
```

| Feature param | Role | Service |
|---------------|------|---------|
| `recommendation` | TA | `RecommendationService` |
| `missingSkills` | TA | `MissingSkillService` |
| `skillMatch` | TA | `SkillMatchService` |
| `moWorkloadAdvice` | MO | `WorkloadAdviceService` |

Default provider: **mock** (offline, deterministic). HTTP provider activates when `LM_BASE_URL` and `LM_API_KEY` are set.

---

## 5. Key design decisions

| Decision | Rationale |
|----------|-----------|
| Servlet/JSP over SPA | Course stack requirement; simple deployment |
| Repository pattern | Testability; isolates SQL from business logic |
| Flyway migrations | Repeatable schema + seed for dev/demo |
| CV on filesystem | Large binaries outside DB; path in `profiles.cv_file_name` |
| Pluggable LM client | Demo without API keys; swap vendor in one class |
| `ServiceFactory` | Single composition root for servlet `init()` |

---

## 6. Related documents

- [REQUIREMENTS.md](REQUIREMENTS.md) — functional and NFR summary
- [DEPLOYMENT.md](DEPLOYMENT.md) — runtime topology
- [TEST_PLAN.md](TEST_PLAN.md) — verification strategy
