# Release Notes — v1.0.0

**Release date:** 23 May 2026  
**Product:** TA Recruitment System for BUPT International School  
**Team:** EBU6304 Group 51

---

## Highlights

Version 1.0.0 is the first production-oriented release of the TA recruitment web application. It replaces file-based storage with **PostgreSQL**, adds **BCrypt** authentication, optional **SMTP** notifications, and **AI-assisted** job matching for applicants and module organisers.

---

## What's new

### For TA applicants
- Register, log in, and maintain an English profile with education history and skills.
- Upload and replace CV files (stored on server filesystem).
- Browse, search, filter, and sort published TA jobs.
- Save favorites and view recently viewed jobs (last 5).
- Submit applications with CV and deadline validation.
- Track application status and read MO feedback.
- AI job recommendations and missing-skills guidance on the job page (offline mock by default).

### For module organisers
- Create draft and published TA vacancies with skills, schedule, and deadlines.
- Review applicants for **your** postings; open read-only profiles and CVs.
- Accept or reject applications with optional feedback.
- Email notification when SMTP is configured.
- AI workload advice during assignment decisions.

### For administrators
- Dashboard with recruitment and workload statistics.
- Manage users: deactivate, delete, create MO accounts.
- View/delete job postings; browse TA profiles; download CVs.
- Export users, jobs, and applications as CSV.
- Override application status (force accept/reject/pend).
- Admin AI demo page for prompt testing.

### Platform
- Flyway database migrations on startup.
- HikariCP connection pooling.
- Audit log table for accountability.
- Password reset token flow (email when SMTP configured).
- ~230+ automated JUnit tests.

---

## Upgrade notes

If upgrading from the JSON prototype branch:

1. Provision PostgreSQL and set `DB_*` environment variables.
2. Run `mvn clean package` and deploy fresh WAR — Flyway creates schema and seed data.
3. Migrate any custom JSON data manually or re-seed demo accounts.
4. Move CV files into `WEB-INF/data/cv/{userId}/` matching `profiles.cv_file_name`.
5. Rotate all demo passwords before exposing to a network.

---

## Known limitations

| Area | Limitation |
|------|------------|
| US-23 | No application priority reorder |
| US-10 | No self-service profile delete |
| US-05–06 | Password reset requires SMTP for full email flow |
| US-29 | MO skill matching partial (AI demo + workload advice) |
| Export | CSV only (no Excel) |
| CSRF | Not fully enforced — use HTTPS and network controls in production |
| Docker | Compose file documented; add to repo for one-command deploy |

See [GAP_ANALYSIS.md](GAP_ANALYSIS.md) for details.

---

## Demo access

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@bupt.local` | `admin123` |
| MO | `mo@bupt.local` | `mo123` |
| TA | `alice.chen@bupt.local` | `ta123` |

Full account list: [USER_MANUAL.md](USER_MANUAL.md).

---

## Documentation

| Document | Purpose |
|----------|---------|
| [REQUIREMENTS.md](REQUIREMENTS.md) | SRS summary (US-01–US-35) |
| [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) | Architecture and ERD |
| [TEST_PLAN.md](TEST_PLAN.md) | Test strategy and matrix |
| [USER_MANUAL.md](USER_MANUAL.md) | Role workflows |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Install and env vars |
| [PRIVACY.md](PRIVACY.md) | Personal data policy |
| [CHANGELOG.md](../CHANGELOG.md) | Version history |

---

## Support

Coursework project — contact Group 51 via course channels. For defects, open a GitHub Issue with steps to reproduce and `mvn test` output.
