# Release Notes — v3.0.0-preview

> **Status: Preview draft — not an official release.**  
> For team review before tagging `v3.0.0` or publishing on GitHub Releases.  
> **Baseline:** [`v2.0.0`](https://github.com/markrivera683/Recruitment-System-for-TA/releases/tag/v2.0.0) → current `main` (`ad4070c`, 2026-05-22)

---

## Highlights

- **MO AI workload advisor** — streaming AI suggestions on the Module Organiser dashboard to help review pending applications against TA workload.
- **Richer TA job browsing** — favorites, recently viewed jobs, live application counts, and clear feedback when TA slots are full.
- **Maven-first project layout** — standard Java structure with GitHub Actions CI on every push/PR to `main`.
- **TA AI matching fixes** — corrected recommendation logic and job-list UI behaviour introduced after v2.0.

---

## New Features

### Module Organiser (MO)

- **AI workload advice** (`moWorkloadAdvice` via SSE)
  - New `WorkloadService`, `WorkloadAdviceService`, and `MoWorkloadSnapshot`
  - Pending applications on the MO dashboard show workload chips and inline streaming AI advice (up to 2 concurrent streams)
  - MO-only access; Admin dashboard reuses `WorkloadService` for consistent stats
- **Applicant profile view** — new read-only route `/mo/applicant-profile?userId=` (`MoApplicantProfileServlet`)

### Teaching Assistant (TA)

- **Favorite jobs** (`FavoriteService`, persisted in `favorites.json`)
  - Save / remove favorites from the job list and job detail pages
- **Recently viewed jobs** (`RecentlyViewedService`, persisted in `recently-viewed.json`)
  - Job list surfaces recently opened listings
- **Application stats & slot capacity**
  - New `JobApplicationStats` (pending / accepted / rejected / withdrawn)
  - Job detail shows **Accepted hires: X / Y** and whether new applications are open
  - When slots are full, **Apply for Job** shows a visible error banner and click feedback (no silent disabled button)

### Demo data

- Additional demo TA profiles and sample applications for coursework demos and manual testing (PR #67)

---

## Improvements

### Profile & CV

- Profile page UI polish (PR #67)
- Stronger profile field validation (PR #62)
- CV delete and view/download button fixes
- Registration and profile save flow refinements

### Job list & applications

- Job list loading states and clearer empty states (PR #64)
- Clearer redirect messages for submit success, duplicate applications, and validation errors
- MO dashboard deadline field uses text input (`yyyy-mm-dd`) to avoid locale-specific date-picker labels
- Minor UI colour adjustments (PR #70)

### AI / LM integration

- **TA job list:** mock recommendations now rank by keyword relevance instead of list order
- **TA job list:** AI matching accepts skills **or** completed courses; explicit error when profile input is insufficient
- **TA job list:** no auto-selection of the first job; AI panel appears only when the profile is ready
- `ProfileService`: added `hasAiMatchingInput()` and `buildAiCapabilityText()`

---

## Bug Fixes

| Issue | Fix |
|-------|-----|
| MO dashboard JSP 500 (duplicate `pending` variable) | Renamed to `acceptedCount`, `pendingCount`, `potentialLoad` |
| No feedback when applying to a full TA slot | Error banner + clickable blocked Apply button with message |
| LM recommendation logic and jobs page layout | `MockLmClient` matching + `jobs.jsp` layout hotfix (#71) |
| JSP / Tomcat compatibility | JSP compilation fixes (PR #66) |
| Application status change edge cases | Status update logic fix (PR #61) |

---

## Engineering & Maintenance

### Maven migration (breaking change)

- Removed legacy nested `src/src/` layout and root-level manual compile scripts
- Standard layout: `src/main/java`, `src/main/webapp`, `src/test/java`
- Build: `mvn clean package` · Test: `mvn test` · Run: `mvn tomcat7:run-war`

### CI/CD

- **GitHub Actions** (`.github/workflows/maven.yml`) — JDK 11, `mvn verify`
- **Release Drafter** (`.github/release-drafter.yml`) — draft release notes from merged PRs

### Tests

- Added / extended: `WorkloadServiceTest`, `WorkloadAdviceServiceTest`, `MockLmClientTest`, and related AI pipeline tests
- Full suite: `mvn test` passes

### Documentation

- Updated `README.md` (Maven structure, routes, AI configuration)
- Updated `docs/ProductBacklog_group51.xlsx` (PRs #57–#60)

---

## Upgrade Notes (from v2.0.0)

1. Requires **JDK 11+** and **Maven 3.9+**
2. Build: `mvn clean package`
3. Run locally: `mvn tomcat7:run-war` → `http://localhost:18080/ta-recruitment/login`
4. Legacy compile/run scripts are deprecated; use `pom.xml` workflows only
5. New runtime data files (seeded or created on first use):
   - `favorites.json`
   - `recently-viewed.json`

---

## Contributors

| Member | Main contributions |
|--------|-------------------|
| Ruiyang Sun | Maven migration, MO AI workload advice, CI / Release Drafter, application feedback & slot control, LM fixes |
| Qinchun Chen | Favorite jobs, recently viewed jobs, backlog updates |
| Tianjing Zhuang | Profile UI & validation, JSP compatibility, demo data |
| Qixin Li | CV features, status changes, UI colours, backlog |

---

## Merged PRs since v2.0.0

| PR | Summary |
|----|---------|
| #57–#60 | Product backlog updates |
| #61 | Application status change fix |
| #62 | Profile field validation |
| #63 | Favorite jobs |
| #64 | Application feedback, job loading states, application stats |
| #65 | Recently viewed jobs |
| #66 | JSP compatibility |
| #67 | Profile UI + demo data |
| #69 | MO AI workload advisor |
| #70 | UI colour fixes |
| #71 | LM logic & layout hotfix |

---

## Publishing checklist (when ready for official release)

- [ ] Final review of this preview text
- [ ] Confirm version tag (`v3.0.0` vs `v3.0.0-rc.1`)
- [ ] Run `mvn clean verify` on `main`
- [ ] Tag and push: `git tag -a v3.0.0 -m "v3.0.0"` / `git push origin v3.0.0`
- [ ] Create **GitHub Release** (can start from Release Drafter draft) and paste the final notes
- [ ] Remove or archive this `-preview` file, or rename to the official release notes path
