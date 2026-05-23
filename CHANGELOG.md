# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/your-org/Recruitment-System-for-TA/compare/v1.0.0...HEAD)

### Added

- JSON persistence for audit logs (`audit-logs.json`) and password reset tokens (`password-reset-tokens.json`).
- `AppInitListener` and `FileTestSupport` for file-based startup and tests.

### Changed

- **Coursework compliance:** removed PostgreSQL, Flyway, HikariCP, H2, and JDBC repository layer.
- Restored `FileStore`-backed services for users, profiles, applications, favorites, recently viewed, jobs, audit, and reset tokens.
- Simplified Docker Compose (Tomcat + MailHog only; data volume for `WEB-INF/data/`).
- Updated deployment and architecture documentation for JSON/text file storage.

### Removed

- `DatabaseManager`, `DatabaseInitListener`, `com.bupt.ta.repository` package, Flyway SQL migrations.

---

## [1.0.0](https://github.com/your-org/Recruitment-System-for-TA/releases/tag/v1.0.0) - 2026-05-23

### Added

- PostgreSQL persistence with Flyway migrations (`V1__schema.sql`, `V2__seed_data.sql`).
- Repository layer and `ServiceFactory` wiring via HikariCP.
- BCrypt password hashing (`PasswordHasher`) with legacy plaintext fallback.
- Password reset flow (`ForgotPasswordServlet`, `ResetPasswordServlet`, `PasswordResetService`).
- SMTP notifications (`NotificationService`) for status changes and reset emails when configured.
- Audit logging (`AuditService`, `audit_logs` table).
- TA AI features: job recommendations, missing skills, SSE streaming (`AiStreamServlet`).
- MO workload advice AI; admin AI demo page.
- Favorites and recently viewed jobs (max 5).
- Admin CSV export, TA profile listing, CV download, application status override.
- Admin MO account creation.
- Integration tests with H2 (`UserLifecycleIntegrationTest`, `ApplicationFlowIntegrationTest`).
- Phase 4 documentation: requirements, design, test plan, user manual, deployment, privacy, iteration log, release notes.
- MIT LICENSE.

### Changed

- Migrated from JSON `FileStore` to JDBC repositories for users, profiles, jobs, applications.
- MO dashboard scoped to jobs created by logged-in MO.
- Applicant routes guarded with `ensureTa()` for role separation.
- Apply validation: CV required; deadline enforced when set.
- Seed passwords stored as BCrypt hashes in database seed script.

### Deprecated

- JSON data files under `WEB-INF/data/*.json` for runtime persistence (retained only for legacy tests/reference).

### Security

- Passwords hashed at rest for new and seeded accounts.
- Password reset tokens expire and are single-use.
- Generic forgot-password response to prevent account enumeration.

### Documentation

- `docs/TRACEABILITY.md` and `docs/GAP_ANALYSIS.md` for backlog alignment.
- Restructured README with Quick Start and demo account table.

