# Demo Script — 10-Minute Commercial Walkthrough

Use after `docker compose up` or local Tomcat deploy. Base URL: `http://localhost:8080/ta-recruitment/login`

## Preparation (1 min)

- Confirm demo accounts in `WEB-INF/data/users.json` work (login page hints).
- Optional: open MailHog UI at `http://localhost:8025` for password-reset demo.

## Act 1 — TA Applicant (3 min)

1. **Login** as `alice.chen@bupt.local` / `ta123`.
2. **Profile** (`/profile`): show complete profile + CV upload.
3. **Job portal** (`/job`): search/filter, open job detail, show AI recommendation panel.
4. **Apply** from job detail or applications page.
5. **Track status** on `/applications` (Pending).

*Talking point:* Deadline and CV rules enforced; AI is advisory (mock by default).

## Act 2 — Module Organiser (3 min)

1. **Logout**, login as `mo@bupt.local` / `mo123`.
2. **Dashboard** (`/mo`): only MO's own jobs and related applications.
3. **Create/publish** a vacancy (deadline, skills, capacity).
4. **Review** applicant profile link → CV download.
5. **Accept or reject** with feedback; mention email notification if SMTP configured.

*Talking point:* MO cannot approve applications for another MO's module.

## Act 3 — Administrator (2 min)

1. **Logout**, login as `admin@bupt.local` / `admin123`.
2. **Dashboard** (`/admin`): workload stats, user list.
3. **Create MO user** (Users tab) or deactivate account.
4. **Export CSV** (users or applications).
5. **Override status** via applications-by-status lists.

## Act 4 — Security & Architecture (1 min)

- BCrypt passwords, CSRF on forms, role guards.
- JSON file storage under `WEB-INF/data/`; CV files on disk; **no database**.
- Pluggable AI (`LM_PROVIDER=mock` default).

## Q&A backup

- Docs index: README → Documentation table.
- Tests: `mvn test` (320+ cases).
- Deploy: [DEPLOYMENT.md](DEPLOYMENT.md).
