# Software Requirements Specification (Summary)

**Project:** TA Recruitment System for BUPT International School  
**Course:** EBU6304 Software Engineering (Group 51)  
**Version:** 1.0.0  
**Source of truth:** [ProductBacklog_group51.xlsx](ProductBacklog_group51.xlsx)

---

## 1. Purpose and scope

The system replaces email/Excel-based TA recruitment with a web application supporting three roles:

| Role | Code | Primary goals |
|------|------|---------------|
| TA Applicant | `TA` | Register, maintain profile/CV, browse jobs, apply, track status |
| Module Organiser | `MO` | Post vacancies, review applicants, approve/reject |
| Administrator | `ADMIN` | User/job oversight, workload monitoring, data export, status override |

Out of scope for v1.0.0: application priority reorder (US-23), self-service profile delete (US-10), LDAP/SSO, mobile app, Excel export (CSV only).

---

## 2. Functional requirements (user stories)

Priority follows MoSCoW from the product backlog. Status reflects v1.0.0 implementation (see [TRACEABILITY.md](TRACEABILITY.md)).

### Authentication and account management

| ID | Story | Priority | Status |
|----|-------|----------|--------|
| US-01 | **TA Registration** — Register with basic information to access applicant features. | Must | Done |
| US-02 | **TA Login** — Secure login to access profile, CV, and applications. | Must | Done |
| US-03 | **TA Logout** — End session on shared devices. | Must | Done |
| US-04 | **Access Control for Applicant Features** — Only authenticated TAs access protected applicant routes. | Must | Partial |
| US-05 | **Request Password Reset** — Request reset via registered email without account enumeration. | Should | Partial |
| US-06 | **Complete Password Reset** — Set new password via single-use expiring token. | Should | Partial |

### Applicant profile and CV

| ID | Story | Priority | Status |
|----|-------|----------|--------|
| US-07 | **Create applicant profile** — Provide personal information and academic background. | Must | Done |
| US-08 | **Edit TA Profile** — Update and save profile fields. | Should | Done |
| US-09 | **View Profile** — TA views own profile; MO views applicant profile read-only. | Should | Done |
| US-10 | **Delete Profile** — Self-service profile removal. | Could | Missing |
| US-11 | **Upload CV** — Upload CV for MO review. | Must | Done |
| US-12 | **Replace CV** — Upload replaces previous CV. | Should | Done |
| US-13 | **View CV** — TA/MO/Admin download or preview CV. | Should | Done |
| US-14 | **Delete CV** — Remove outdated CV files. | Could | Partial |

### Job discovery and applications (TA)

| ID | Story | Priority | Status |
|----|-------|----------|--------|
| US-15 | **Browse available jobs** — List published TA vacancies. | Must | Done |
| US-16 | **Search and filter jobs** — Keyword, module, activity type, skills. | Must | Done |
| US-17 | **View job details** — Full job description and requirements. | Must | Done |
| US-18 | **Sort job listings** — Sort by module name or post date. | Should | Done |
| US-19 | **Save favorite jobs** — Bookmark jobs for later. | Should | Done |
| US-20 | **View recently viewed jobs** — Last 5 viewed jobs. | Should | Done |
| US-21 | **Apply for job** — Submit application with confirmation. | Must | Done |
| US-22 | **Check application status** — Pending / Accepted / Rejected with feedback. | Should | Done |
| US-23 | **Applications Manage** — Reorder application priority for MO. | Should | Missing |
| US-24 | **Identify missing skills** — AI highlights skill gaps vs job requirements. | Could | Done |
| US-25 | **AI Job Recommendation** — AI ranks suitable jobs from profile/CV. | Could | Done |

### Module organiser

| ID | Story | Priority | Status |
|----|-------|----------|--------|
| US-26 | **Post job** — Create draft and publish TA vacancy with skills, deadline, workload. | Must | Done |
| US-27 | **Select applicants** — Approve/reject with optional email notification. | Must | Partial |
| US-28 | **View Applicant List** — List applicants per vacancy with status. | Must | Partial |
| US-29 | **Skill matching (MO)** — AI-assisted candidate matching. | Could | Partial |

### Administrator

| ID | Story | Priority | Status |
|----|-------|----------|--------|
| US-30 | **View TA workload** — Monitor assignments and overload warnings. | Should | Done |
| US-31 | **Balance TA workload** — AI suggests lower-workload TAs during assignment. | Should | Partial |
| US-32 | **Manage users** — List, deactivate, delete; create MO accounts. | Must | Partial |
| US-33 | **Manage job postings** — View and delete jobs; MO edits own postings. | Should | Partial |
| US-34 | **Export recruitment data** — CSV export of users, jobs, applications. | Could | Done |
| US-35 | **Override application status** — Admin force-accept/reject/pend from status lists. | Should | Done |

---

## 3. Non-functional requirements

### 3.1 Security

| Requirement | Target | Implementation |
|-------------|--------|------------------|
| Password storage | Hashed at rest | BCrypt via `PasswordHasher` |
| Session management | Server-side HTTP session | Servlet session; logout invalidates |
| Role-based access | TA / MO / ADMIN route guards | `BaseServlet` role checks, `ensureTa()` |
| Sensitive data | Minimise exposure | MO sees applicants for own jobs only (partial) |
| Audit trail | Admin/MO actions logged | `audit_logs` table, `AuditService` |
| CSRF | Form protection | Planned hardening; not fully enforced in v1.0.0 |
| Secrets | No keys in repository | Env vars / local `WEB-INF/*.properties` |

See [PRIVACY.md](PRIVACY.md) for student ID, national ID, and CV handling.

### 3.2 Performance

| Requirement | Target |
|-------------|--------|
| Job list response | &lt; 2 s for up to 500 published jobs on demo hardware |
| Concurrent users | 50 simultaneous sessions (coursework demo scale) |
| AI streaming | SSE deltas within 30 s (`LM_TIMEOUT_MS` default) |
| Database pool | HikariCP, max 10 connections |

### 3.3 Deployment and operability

| Requirement | Target |
|-------------|--------|
| Packaging | Maven WAR (`ta-recruitment.war`) |
| Database migration | Flyway on startup (`V1__schema.sql`, `V2__seed_data.sql`) |
| Configuration | Environment variables with file fallback |
| Container deploy | Docker Compose (PostgreSQL + Tomcat) — see [DEPLOYMENT.md](DEPLOYMENT.md) |
| Backup | PostgreSQL dump + CV file directory |
| HTTPS | Reverse proxy (nginx/Apache) in production |

### 3.4 Usability and maintainability

- English UI for applicant-facing pages.
- Seed/demo accounts for coursework evaluation.
- Javadoc on public API under `com.bupt.ta`.
- Automated test suite (`mvn test`); traceability in [TEST_PLAN.md](TEST_PLAN.md).

---

## 4. Constraints and assumptions

- Java 11, Servlet 4.0, Apache Tomcat 9.x.
- PostgreSQL 14+ for production; H2 for unit/integration tests.
- AI features degrade gracefully: mock provider when API keys absent.
- Email notifications require SMTP configuration; otherwise no-op.

---

## 5. Related documents

| Document | Description |
|----------|-------------|
| [TRACEABILITY.md](TRACEABILITY.md) | Story → code → test mapping |
| [GAP_ANALYSIS.md](GAP_ANALYSIS.md) | Remaining gaps vs backlog |
| [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) | Architecture and data model |
| [USER_MANUAL.md](USER_MANUAL.md) | End-user workflows |
