# Test Plan

**Project:** TA Recruitment System v1.0.0  
**Execution:** `mvn test` from repository root

---

## 1. Test strategy

| Level | Scope | Tools | Location |
|-------|-------|-------|----------|
| **Unit** | Models, services, utilities, AI prompt builders | JUnit 5, Mockito | `src/test/java/com/bupt/ta/**` |
| **Servlet** | HTTP handlers, auth redirects, form handling | JUnit 5, mock requests/responses | `src/test/java/com/bupt/ta/servlet/**` |
| **Integration** | Multi-service flows against temp JSON dirs | JUnit 5, `FileTestSupport` | `src/test/java/com/bupt/ta/integration/**` |
| **UAT** | End-to-end role workflows on deployed WAR | Manual checklist | See [USER_MANUAL.md](USER_MANUAL.md) |

**Pass criteria:** All automated tests green; Must-have user stories (US-01–US-04, US-07, US-11, US-15–US-17, US-21, US-26–US-28, US-32) verified manually or via integration tests.

**CI recommendation:** Run `mvn clean test` on every pull request; optional `mvn package` + smoke deploy.

---

## 2. Test environment

| Component | Unit/Servlet | Integration |
|-----------|--------------|-------------|
| Database | Mocks or none | Temporary JSON directories (`FileTestSupport`) |
| CV storage | Temp directory via `ServiceFactory.fromDataSource` | Temp directory |
| LM provider | Mock (`LM_PROVIDER=mock`) | Mock |
| SMTP | Not invoked in tests | Not invoked |

Shared fixtures: `FileTestSupport`, `ServletTestSupport`, `TestFixtures`, `LmTestSupport`.

---

## 3. User story → test class matrix

| Story | Primary test classes | Type |
|-------|---------------------|------|
| US-01 | `RegisterServletTest`, `AuthServiceTest`, `UserLifecycleIntegrationTest` | Servlet, Unit, Integration |
| US-02 | `LoginServletTest`, `AuthServiceTest` | Servlet, Unit |
| US-03 | `LogoutServletTest` | Servlet |
| US-04 | `BaseServletTest`, `ApplicationServletTest` | Servlet |
| US-05, US-06 | — (manual UAT; `PasswordResetService` untested) | UAT |
| US-07, US-08, US-11, US-12, US-14 | `ProfileServiceTest` | Unit |
| US-09 | `ProfileServiceTest` | Unit + UAT |
| US-10 | — | Not implemented |
| US-13 | — (manual via `CvDownloadServlet`) | UAT |
| US-15–US-18 | `JobServiceTest`, `JobListFiltersTest` | Unit |
| US-19 | `FavoriteServiceTest` | Unit |
| US-20 | `RecentlyViewedServiceTest` | Unit |
| US-21, US-22, US-35 | `ApplicationServiceTest`, `ApplicationServletTest`, `ApplicationFlowIntegrationTest` | Unit, Servlet, Integration |
| US-23 | — | Not implemented |
| US-24 | `MissingSkillServiceTest`, `AiFeatureServiceTest` | Unit |
| US-25 | `RecommendationServiceTest` | Unit |
| US-26–US-28, US-27 | `MoServletTest`, `JobServiceTest` | Servlet, Unit |
| US-29 | `SkillMatchServiceTest` | Unit |
| US-30 | `TaWorkloadStatsTest`, `WorkloadServiceTest` | Unit |
| US-31 | `WorkloadAdviceServiceTest` | Unit |
| US-32 | `AdminUserServletTest`, `AuthServiceTest` | Servlet, Unit |
| US-33 | `JobServiceTest` | Unit |
| US-34 | — (manual `AdminExportServlet`) | UAT |
| AI infrastructure | `MockLmClientTest`, `LmClientFactoryTest`, `HttpLmClientTest`, `LmConfigTest`, … | Unit |

---

## 4. Test inventory by area

### Models
`UserTest`, `ApplicationTest`, `JobApplicationStatsTest`, `TaWorkloadStatsTest`, `TaResumeDisplayTest`, `MoWorkloadSnapshotTest`

### Services
`AuthServiceTest`, `ProfileServiceTest`, `ApplicationServiceTest`, `JobServiceTest`, `FavoriteServiceTest`, `RecentlyViewedServiceTest`, `WorkloadServiceTest`, `FileStoreTest` (legacy)

### AI / LM
`AiFeatureServiceTest`, `SkillMatchServiceTest`, `MissingSkillServiceTest`, `RecommendationServiceTest`, `WorkloadAdviceServiceTest`, `MockLmClientTest`, `LmClientFactoryTest`, `HttpLmClientTest`, plus DTO/config tests under `com.bupt.ta.ai`

### Servlets
`BaseServletTest`, `LoginServletTest`, `RegisterServletTest`, `LogoutServletTest`, `ApplicationServletTest`, `MoServletTest`, `AdminUserServletTest`

### Integration
`UserLifecycleIntegrationTest`, `ApplicationFlowIntegrationTest`

### Utilities
`AppConfigTest`, `ApplicantFieldValidationTest`, `JobListFiltersTest`, `StringsTest`, `HttpJsonClientTest`

---

## 5. UAT checklist (summary)

| # | Scenario | Role | Expected |
|---|----------|------|----------|
| 1 | Register → login → profile save | TA | Account created; profile persisted |
| 2 | Upload CV → apply before deadline | TA | Application Pending; CV required |
| 3 | Filter/sort jobs → favorite → apply | TA | Filters work; favorite saved |
| 4 | MO publish job → approve applicant | MO | Status Accepted; email if SMTP set |
| 5 | Admin export CSV → override status | ADMIN | CSV downloads; status changes persist |
| 6 | AI recommendation on `/job` | TA | SSE stream completes (mock OK) |
| 7 | Password reset flow | TA | Token email or demo message |

Full steps: [USER_MANUAL.md](USER_MANUAL.md).

---

## 6. Known gaps

- No automated tests for password reset servlets or export servlet.
- US-23 (application reorder) not covered — feature missing.
- Global `LM_*` env vars on developer machines may affect `AppConfigTest`; unset for deterministic runs.

---

## 7. Related documents

- [TRACEABILITY.md](TRACEABILITY.md) — story implementation status
- [REQUIREMENTS.md](REQUIREMENTS.md) — acceptance criteria source
