# Deployment Guide

**Application:** TA Recruitment System v1.0.0  
**Artifact:** `target/ta-recruitment.war`

This application uses **no database**. All persistent data is stored as JSON text files under `WEB-INF/data/` (and CV uploads under `WEB-INF/data/cv/{userId}/`).

---

## 1. Prerequisites

| Component | Version |
|-----------|---------|
| JDK | 11+ |
| Maven | 3.9+ |
| Apache Tomcat | 9.x |
| Docker & Compose | Optional, recommended for demo |

---

## 2. Environment variables

Resolution order: **environment variable → JVM `-D` property → `WEB-INF/lm.properties` → default**.

### SMTP (`SMTP_*`)

| Variable | Description | Default |
|----------|-------------|---------|
| `SMTP_HOST` | Mail server hostname | *(empty = email disabled)* |
| `SMTP_PORT` | SMTP port | `587` |
| `SMTP_FROM` | Sender address | *(required with host)* |
| `SMTP_USER` | Auth username | *(optional)* |
| `SMTP_PASSWORD` | Auth password | *(optional)* |

Used by `NotificationService` for password reset and application status emails. When unset, email is a no-op (in-app status still updates).

### Language model (`LM_*`)

| Variable | Description | Default |
|----------|-------------|---------|
| `LM_ENABLED` | Master AI switch | `true` |
| `LM_PROVIDER` | `mock`, `openai`, `custom` | `mock` |
| `LM_API_KEY` | Bearer token | *(empty)* |
| `LM_BASE_URL` | API base URL | *(empty)* |
| `LM_MODEL` | Model name | provider default |
| `LM_TIMEOUT_MS` | HTTP timeout | `30000` |
| `LM_HTTP_CHAT_PATH` | Chat path | `/chat/completions` |

Copy `WEB-INF/lm.properties.example` → `lm.properties` for local overrides. Missing API credentials fall back to `MockLmClient`.

---

## 3. Docker Compose (recommended)

```powershell
mvn clean package
docker compose up --build
```

| Service | URL |
|---------|-----|
| Application | http://localhost:8080/ta-recruitment/login |
| MailHog UI | http://localhost:8025 |

The `app` service mounts volumes for `WEB-INF/data/` and `WEB-INF/data/cv/` so JSON and CV files survive container restarts.

---

## 4. Local Tomcat / Maven plugin

```powershell
mvn clean package
mvn tomcat7:run-war
```

Open http://localhost:18080/ta-recruitment/login

For external Tomcat, deploy `target/ta-recruitment.war` to `{TOMCAT}/webapps/`. Ensure the exploded `WEB-INF/data/` directory is writable for runtime updates.

---

## 5. Data files

| File | Purpose |
|------|---------|
| `users.json` | Accounts (TA, MO, Admin) |
| `profiles.json` | Applicant profiles |
| `jobs.json` | Job postings |
| `applications.json` | TA applications |
| `favorites.json` | Saved jobs per user |
| `recently-viewed.json` | Recent job views |
| `audit-logs.json` | Admin audit trail |
| `password-reset-tokens.json` | Reset tokens |
| `cv/{userId}/*` | Uploaded CV files |

Seed JSON ships in the WAR under `src/main/webapp/WEB-INF/data/`. Back up this directory (and CV subfolder) before upgrades.

---

## 6. Verification checklist

| Check | Expected |
|-------|----------|
| App starts | No errors from `AppInitListener` / `ServiceFactory` |
| Demo login | `admin@bupt.local` / `admin123` works |
| Data writes | New registration appears in `users.json` |
| Tests | `mvn test` passes |

---

## 7. Production notes

1. Replace demo passwords in `users.json` or re-hash with BCrypt.
2. Restrict write access to `WEB-INF/data/` at OS level.
3. Configure HTTPS on Tomcat or a reverse proxy.
4. Set strong SMTP credentials via environment variables (never commit secrets).

See also [SECURITY.md](SECURITY.md) and [PRIVACY.md](PRIVACY.md).
