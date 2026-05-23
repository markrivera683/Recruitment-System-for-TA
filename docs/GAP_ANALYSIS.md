# Gap Analysis — TA Recruitment System v1.0.0

Baseline audit against Product Backlog (US-01 – US-35) and production-readiness criteria.

## Executive summary

| Area | Coursework baseline | v1.0.0 status | Gap severity |
|------|---------------------|---------------|--------------|
| Persistence | JSON files (required) | JSON files + FileStore | **Compliant** |
| Security | Plaintext passwords | BCrypt + CSRF + role guards | Low (demo plaintext seeds remain) |
| MO isolation | Global job/app lists | Filter by `createdByMoId` | Resolved |
| Business rules | Deadline/CV optional | Enforced at apply time | Resolved |
| Password reset | UI placeholder | Token JSON + optional email | Resolved |
| Notifications | None | SMTP email on status change | Optional |
| Documentation | README only | Full SE doc pack | Resolved |
| UX | No demo hints | Quick Start, honest reset UI | Resolved |

## Non-functional gaps (production beyond coursework)

| NFR | Current | Production target |
|-----|---------|-------------------|
| Concurrency | Read-modify-write JSON races | File locking or DB if scaled |
| Deploy | WAR + Tomcat / Docker | Same; backup `WEB-INF/data/` |
| Audit | `audit-logs.json` | Tamper-evident store if required |
| Compliance | `docs/PRIVACY.md` | Encrypt ID fields at rest |

## Out of scope for v1.0.0

- Application priority reorder (US-23)
- Self-service profile delete (US-10)
- LDAP/SSO, mobile app, video interviews
- Excel export (CSV only)
- Relational database (explicitly excluded by coursework spec)

## Traceability

See [TRACEABILITY.md](TRACEABILITY.md) for per-story implementation mapping.
