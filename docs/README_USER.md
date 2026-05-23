# User Manual

**System:** TA Recruitment System v1.0.0  
**URL (local):** [http://localhost:18080/ta-recruitment/login](http://localhost:18080/ta-recruitment/login)

---

## 1. Demo accounts

Seed data from Flyway `V2__seed_data.sql`. **Change or remove before production.**


| Role             | Email                     | Password   | Notes                                   |
| ---------------- | ------------------------- | ---------- | --------------------------------------- |
| Administrator    | `admin@bupt.local`        | `admin123` | Full admin dashboard                    |
| Module Organiser | `mo@bupt.local`           | `mo123`    | Owns seeded job postings                |
| TA Applicant     | `alice.chen@bupt.local`   | `ta123`    | Profile + sample application (Accepted) |
| TA Applicant     | `brian.li@bupt.local`     | `ta123`    | ML/statistics skills                    |
| TA Applicant     | `clara.wang@bupt.local`   | `ta123`    | Physics lab background                  |
| TA Applicant     | `daniel.zhang@bupt.local` | `ta123`    | Writing/invigilation                    |
| TA Applicant     | `19131091012@163.com`     | `1234`     | Team member demo account                |


New self-registrations receive role `TA`.

---

## 2. TA applicant workflow

### Step 1 — Register or log in

1. Open `/register` or use a demo account on `/login`.
2. Registration requires name, student ID, unique email, and password.
3. After login you are redirected to the applicant area.

### Step 2 — Complete profile and upload CV

1. Go to **Profile** (`/profile`).
2. Fill personal fields: full name, gender, degree, major, student ID, national ID, phone, email.
3. Add education entries, courses, availability, and skills.
4. Upload a CV (PDF/DOC, max ~10 MB). Save the form.
5. Preview your CV via **Download CV** (`/cv`).

### Step 3 — Browse and filter jobs

1. Open **Jobs** (`/job`).
2. Use search, module filter, activity type, and skills filters.
3. Sort by module name or post date.
4. Click a job for details; view **Favorites** and **Recently viewed** (last 5).

### Step 4 — Apply for a job

1. From job detail, click **Apply**.
2. Application requires a uploaded CV and must be before the deadline (if set).
3. Confirmation appears; status starts as **Pending**.

### Step 5 — Track applications and use AI helpers

1. Open **My Applications** (`/applications`).
2. Filter by status: Pending, Accepted, Rejected; read MO feedback.
3. On the job page, use **AI recommendations** and **Missing skills** panels (streaming; works offline with mock AI).

---

## 3. Module organiser (MO) workflow

### Step 1 — Log in

Use `mo@bupt.local` / `mo123` or an MO account created by admin.

### Step 2 — Open MO dashboard

Go to `/mo`. You see **your** job postings and applicants (scoped to jobs you created).

### Step 3 — Create and publish a vacancy

1. Enter module name, code, activity type, description, required skills, deadline, schedule, and TA count.
2. **Save** as draft or **Publish** to make visible to TAs.

### Step 4 — Review applicants

1. Select a job to view the applicant list with status.
2. Click an applicant name to open read-only profile (`/mo/applicant-profile?userId=...`).
3. Download CV from the profile view when available.

### Step 5 — Approve or reject

1. Choose **Accept** or **Reject**; optional feedback text.
2. Applicant status updates immediately.
3. If SMTP is configured, notification email is sent; otherwise status change is in-app only.
4. Optional: use **Workload advice** AI stream when assigning (MO feature on dashboard).

---

## 4. Administrator workflow

### Step 1 — Log in

Use `admin@bupt.local` / `admin123`.

### Step 2 — Dashboard overview

Open `/admin` for user counts, job stats, TA workload summary, and quick links.

### Step 3 — Manage users

1. Go to `/admin/users`.
2. View all accounts; deactivate or delete users.
3. Create new MO accounts (name, email, password).

### Step 4 — Manage jobs and profiles

1. `/admin/jobs` — view and delete any posting.
2. `/admin/ta-profiles` — browse applicant profiles.
3. `/admin/cv?userId=` — download applicant CVs.

### Step 5 — Export data and override decisions

1. `/admin/export` — download CSV (users, jobs, applications).
2. Use status-filtered application lists to **Force accept**, **Force reject**, or **Force pend** when correcting MO decisions.
3. Optional: `/admin/ai-demo` for manual AI prompt testing (admin only).

---

## 5. Password reset

1. **Forgot password** (`/forgot-password`) — enter registered email; generic success message (no account enumeration).
2. If SMTP is configured, check email for reset link; otherwise use demo/token from server logs in development.
3. **Reset password** (`/reset-password?token=...`) — set new password; token is single-use and expires.

---

## 6. Logout

Click **Logout** or visit `/logout` on any authenticated page. Session is destroyed; browser back button must not restore access.

---

## 7. Troubleshooting


| Issue           | Action                                                            |
| --------------- | ----------------------------------------------------------------- |
| Cannot apply    | Ensure CV uploaded and deadline not passed                        |
| Empty job list  | MO must publish jobs; check filters                               |
| AI panel empty  | Default mock AI should work; check `LM_ENABLED=true`              |
| No reset email  | Configure `SMTP_`* variables — see [DEPLOYMENT.md](DEPLOYMENT.md) |
| 403 on TA pages | Log in as TA role, not MO/ADMIN                                   |


---

## 8. Related documents

- [REQUIREMENTS.md](REQUIREMENTS.md) — full user stories
- [PRIVACY.md](PRIVACY.md) — personal data handling
- [DEPLOYMENT.md](DEPLOYMENT.md) — installation

