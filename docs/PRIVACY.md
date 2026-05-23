# Privacy and Data Protection

**System:** TA Recruitment System v1.0.0  
**Audience:** Applicants, module organisers, administrators, and deployers

This document describes how the system collects, stores, uses, and protects personal data in the coursework deployment. It is **not** a substitute for institutional legal review before production use at BUPT or QMUL.

---

## 1. Data controller context

The recruitment system processes personal data on behalf of BUPT International School TA recruitment. Deployers act as technical administrators; module organisers access applicant data for hiring decisions only.

---

## 2. Categories of personal data

| Category | Fields | Purpose | Stored in |
|----------|--------|---------|-----------|
| Account | Name, email, student ID, password hash | Authentication, identification | `users` table |
| Profile | Full name, gender, degree, major, phone, email, education, courses, availability, skills | Recruitment assessment | `profiles` table |
| **Student ID** | `student_id` on user and profile | Verify enrolment; link to academic records | `users`, `profiles` |
| **National ID** | `id_card` (national ID card number) | Identity verification for hiring compliance (demo) | `profiles.id_card` |
| **CV files** | Uploaded documents (PDF, DOC, etc.) | Qualification review by MO/Admin | Filesystem `WEB-INF/data/cv/{userId}/`; filename in `profiles.cv_file_name` |
| Application | Module, role, date, status, feedback | Recruitment workflow | `applications` table |
| Audit | Actor, action, target, timestamp | Accountability | `audit_logs` table |
| Reset tokens | Token, expiry | Password recovery | `password_reset_tokens` table |

AI features send **skills, job requirements, and profile summaries** to the configured LM provider. Default **mock** provider keeps data on-server. HTTP providers transmit prompt text to third-party APIs — configure only with institutional approval.

---

## 3. Legal basis and purpose limitation

Data is collected **only** for TA recruitment: evaluating suitability, scheduling, and administrative reporting. Do not reuse applicant data for unrelated marketing or research without separate consent.

---

## 4. Who can access data

| Role | Access |
|------|--------|
| **TA** | Own profile, CV, applications, favorites |
| **MO** | Applicants who applied to **MO's own** job postings; read-only profile and CV |
| **ADMIN** | All users, profiles, CVs, applications; export CSV |
| **System** | Automated email (if SMTP configured); audit logging |

Unauthenticated users cannot access protected routes. Session cookies identify logged-in users.

---

## 5. Student ID handling

- Collected at registration and profile edit; synced to `users.student_id` where applicable.
- Displayed to the applicant on their profile page.
- Visible to MO when reviewing that applicant's submission and to administrators.
- **Minimisation:** Do not expose student ID on public job listings or AI output shown to other applicants.
- **Retention:** Retained while the account is active; deleted when admin deletes the user (cascade on related rows).

---

## 6. National ID (`id_card`) handling

- Optional in demo; may be required in a real institutional deployment.
- Stored in PostgreSQL `profiles.id_card` as plain text — **encrypt at rest or tokenise** before any production deployment handling real national IDs.
- Restrict display to MO/Admin applicant review screens; never include in CSV export columns unless legally required and approved.
- Applicants should be informed why national ID is requested and how long it is kept.

---

## 7. CV file handling

| Topic | Practice |
|-------|----------|
| Upload | Multipart form on `/profile`; max ~10 MB |
| Storage | Server filesystem, not database BLOB |
| Access | Owner via `/cv`; MO via applicant profile; Admin via `/admin/cv` |
| Replace | New upload overwrites filename reference; old file may remain on disk until cleanup |
| Delete | Partial support via profile delete-CV action; orphaned files should be purged by admin maintenance |
| Backup | Include CV directory in backup policy alongside database |
| Transfer | CVs may contain third-party personal data; applicants responsible for content they upload |

CVs must not be sent to external AI APIs unless explicitly disclosed and contractually covered. Current mock AI uses skills text from profile, not raw CV binary.

---

## 8. Security measures

- Passwords stored as BCrypt hashes (`password_hash`).
- HTTPS recommended in production (see [DEPLOYMENT.md](DEPLOYMENT.md)).
- Role checks on servlets; MO scoped to own jobs.
- Audit log for sensitive admin/MO actions.
- Secrets (DB, SMTP, LM API keys) via environment variables, not committed to Git.

**Gaps for production:** field-level encryption for national ID, CSRF tokens, formal data retention schedule, GDPR/PIPL consent flows.

---

## 9. Retention and deletion

| Data | Default retention (coursework) | Deletion |
|------|-------------------------------|----------|
| Active account | Until admin deactivates/deletes | Admin user delete cascades profile, applications |
| CV files | With account | Remove files under `cv/{userId}/` on account delete (manual verify) |
| Audit logs | Indefinite in demo | Purge policy TBD for production |
| Reset tokens | Until used or expired | Deleted after successful reset |

Applicants cannot fully self-delete profiles in v1.0.0 (US-10 not implemented). Contact administrator for erasure requests.

---

## 9. Data subject rights (informal)

Applicants may:

- **Access** — view profile and applications when logged in.
- **Rectify** — edit profile and replace CV.
- **Erasure** — request admin account deletion (manual process).
- **Portability** — no self-service export; admin CSV may include applicant fields.

Formal DSAR processes must follow school policy.

---

## 10. International transfers

If `LM_PROVIDER` uses an HTTP API outside your jurisdiction, prompt data may cross borders. Use mock provider for demos; obtain approval before enabling cloud LLM APIs with real applicant data.

---

## 11. Contact

For coursework: Group 51 via EBU6304 channels.  
For production deployment: designate a school data protection contact before go-live.

---

## 12. Related documents

- [REQUIREMENTS.md](REQUIREMENTS.md) — security NFRs
- [USER_MANUAL.md](USER_MANUAL.md) — what users enter in forms
- [DEPLOYMENT.md](DEPLOYMENT.md) — secure deployment
