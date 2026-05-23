# Security Design — TA Recruitment System v1.0.0

## Threat model (summary)

| Threat | Mitigation |
|--------|------------|
| Credential theft | BCrypt password hashes; session invalidation on login |
| CSRF on state-changing POST | `CsrfFilter` + hidden `csrfToken` on all forms |
| Session fixation | New session after successful login |
| Horizontal privilege (TA routes) | `BaseServlet.ensureTa()` on applicant endpoints |
| MO approves others' jobs | `applicationBelongsToMo()` + `createdByMoId` checks |
| Path traversal on CV download | Normalized path + prefix check under `cv/{userId}/` |
| SQL injection | Parameterized JDBC in repositories |
| API key leakage | LM keys via env / `lm.properties` (gitignored) |

## Password policy

- Minimum length enforced at registration/reset (see `ApplicantFieldValidation` / servlets).
- Stored as BCrypt (`PasswordHasher`); legacy plaintext accepted once then re-hashed on login.
- Seed/demo passwords documented in USER_MANUAL — must be changed for production.

## CSRF

- Filter: `com.bupt.ta.security.CsrfFilter` mapped to `/*`.
- Session attribute `_csrfToken` generated on first request.
- POST requests must include matching `csrfToken` parameter (except safe GET-only endpoints).

## Session management

- Attribute `user` holds logged-in `User` after authentication.
- `LoginServlet` invalidates prior session and creates a new one on success.
- `LogoutServlet` destroys session.

## Role-based access

| Role | Guard | Endpoints |
|------|-------|-----------|
| ADMIN | `ensureAdmin()` | `/admin/**` |
| MO | `ensureMo()` | `/mo/**` |
| TA | `ensureTa()` | `/job`, `/profile`, `/applications`, TA AI stream |

## Transport and deployment

- Production deployments should terminate TLS at reverse proxy (see [DEPLOYMENT.md](DEPLOYMENT.md)).
- Database credentials via `DB_URL`, `DB_USER`, `DB_PASSWORD` — never committed.
- SMTP credentials via `SMTP_*` environment variables.

## Audit

- Sensitive actions logged to `audit_logs` via `AuditService` (approve/reject, admin overrides, user create/delete).

## Known limitations (v1.0.0)

- No MFA / LDAP / SSO.
- Rate limiting not implemented on login (recommended for public deployment).
- Email notifications require external SMTP configuration.
