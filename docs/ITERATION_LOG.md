# Iteration Log

**Project:** TA Recruitment System — Group 51  
**Method:** Agile (4 development phases aligned with coursework sprints)

Phases map to repository milestones: JSON prototype → hardened access control → PostgreSQL production baseline → documentation and release.

---

## Iteration 1 — Core TA and admin foundation

**Goals**

- Deliver Must-have TA flows: register, login, logout, profile, CV upload, job browse, apply.
- Admin user listing and basic job oversight.
- JSON file persistence and Servlet/JSP UI skeleton.

**Outcomes**

- Servlets: `RegisterServlet`, `LoginServlet`, `LogoutServlet`, `ProfileServlet`, `JobServlet`, `ApplicationServlet`, admin entry points.
- Services: `AuthService`, `ProfileService`, `JobService`, `ApplicationService` with `FileStore`.
- Seed accounts and sample jobs in `WEB-INF/data/*.json`.
- Initial JUnit coverage for auth, profile, jobs, applications.

**Stories primarily addressed:** US-01, US-02, US-03, US-07, US-11, US-15, US-17, US-21, US-32, US-33, US-34.

---

## Iteration 2 — MO workflows and applicant UX

**Goals**

- Module organiser dashboard: post, publish, review applicants, approve/reject.
- Job search, filter, sort for TAs.
- Role-based access: restrict TA-only routes with `ensureTa()`.
- Expand automated tests (servlets, filters).

**Outcomes**

- `MoServlet`, `MoApplicantProfileServlet`, `JobListFilters`.
- Application status tracking (US-22).
- Partial MO isolation and access control improvements (US-04, US-27, US-28).
- AI scaffold: `com.bupt.ta.ai`, mock LM client, admin AI demo page.
- Workload stats for admin dashboard (US-30).

**Stories primarily addressed:** US-04 (partial), US-16, US-18, US-22, US-26–US-29 (partial), US-30.

---

## Iteration 3 — Production readiness

**Goals**

- Replace JSON storage with PostgreSQL + Flyway migrations.
- BCrypt password hashing; password reset token flow.
- SMTP notifications on status change.
- Enforce apply rules (CV required, deadline).
- MO scope filtering; admin create MO; audit logging.

**Outcomes**

- Packages: `persistence`, `repository`, `security`.
- Schema `V1__schema.sql`, seed `V2__seed_data.sql`.
- `PasswordResetService`, `NotificationService`, `AuditService`.
- HikariCP connection pool; `ServiceFactory` composition root.
- Integration tests with temporary JSON data dirs (`FileTestSupport`).
- TA-facing AI on `/job` and SSE endpoint `/api/ai/stream`.
- Favorites and recently viewed (US-19, US-20).
- Admin application status override (US-35).

**Stories primarily addressed:** US-05–US-06 (partial), US-12–US-14 (partial), US-19, US-20, US-24, US-25, US-27–US-28 (partial), US-31 (partial), US-32 (partial), US-35.

---

## Iteration 4 — Documentation and v1.0.0 release

**Goals**

- Complete software engineering documentation pack (SRS summary, design, test plan, user manual, deployment, privacy).
- Restructure README with Quick Start and documentation index.
- CHANGELOG, release notes, MIT LICENSE.
- Gap analysis and traceability matrices for assessment.

**Outcomes**

- `docs/REQUIREMENTS.md`, `SYSTEM_DESIGN.md`, `TEST_PLAN.md`, `USER_MANUAL.md`, `DEPLOYMENT.md`, `PRIVACY.md`, `ITERATION_LOG.md`.
- `CHANGELOG.md`, `docs/RELEASE_NOTES_v1.0.0.md`, `LICENSE`.
- Updated `TRACEABILITY.md`, `GAP_ANALYSIS.md`.
- README reflects PostgreSQL stack and links to backlog xlsx (PDFs not in repo).

**Remaining gaps (honest v1.0.0 scope):** US-10, US-23; partial US-04, US-05–US-06, US-27–US-28, US-29, US-31–US-33; Docker Compose documented as recommended pattern.

---

## Velocity summary

| Iteration | Focus | Must-haves closed |
|-----------|-------|-------------------|
| 1 | TA core + JSON | ~8 |
| 2 | MO + filters + AI scaffold | +4 |
| 3 | DB + security + notifications | +6 (incl. partial fixes) |
| 4 | Docs + release | 0 new Must — quality gate |

---

## Related documents

- [ProductBacklog_group51.xlsx](ProductBacklog_group51.xlsx) — sprint assignments per story
- [TRACEABILITY.md](TRACEABILITY.md) — current implementation status
- [GAP_ANALYSIS.md](GAP_ANALYSIS.md) — open gaps
