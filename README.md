# Recruitment System for TA

This repository contains the group project for **EBU6304 Software Engineering**.
The project aims to develop a **Teaching Assistant Recruitment System** for BUPT International School to improve the efficiency of the TA recruitment process.

---

## Project Overview

The current TA recruitment process mainly relies on email and Excel files, which may lead to fragmented information, repeated manual work, and difficulty in tracking applications. This project proposes a lightweight recruitment system that allows:

- **TA Applicants** to register, create profiles, upload CVs, browse job postings, submit applications, and track application status online
- **Module Organisers** to post TA jobs and review incoming applications
- **Administrators** to monitor overall recruitment information and workload

The system is developed following **Agile software development methods**.

---

## Tech Stack


| Layer         | Technology                                                           |
| ------------- | -------------------------------------------------------------------- |
| Language      | Java 11                                                              |
| Web Framework | Java Servlet 4.0 / JSP 2.3                                           |
| Server        | Apache Tomcat 9.x                                                    |
| Data Storage  | Plain JSON files (`.json`) — no database                             |
| Build         | Maven (`mvn clean package`, `mvn test`, `mvn tomcat7:run-war`) |
| Dependencies  | `javax.servlet-api 4.0.1` (provided by Tomcat), `jstl 1.2`           |


---

## Repository Structure

```text
Recruitment-System-for-TA/          # Repository root / Maven root
├── docs/                           # Project documents
│   ├── ProductBacklog_group51.xlsx
│   ├── Prototype_group51.pdf
│   └── Report_group51.pdf
├── pom.xml                         # Maven build (compile, test, WAR, run)
├── src/
│   ├── main/
│   │   ├── java/com/bupt/ta/
│   │   │   ├── ai/                 # LM client interfaces, mock + HTTP scaffold
│   │   │   ├── model/              # Data models (User, Job, Application, …)
│   │   │   ├── service/            # Business logic & file persistence
│   │   │   │   └── ai/             # AiFeatureService + feature builders
│   │   │   ├── util/               # AppConfig, HttpJsonClient, Strings
│   │   │   └── servlet/            # HTTP handlers (@WebServlet + web.xml)
│   │   └── webapp/
│   │       ├── static/css/         # app.css, admin-dashboard.css
│   │       ├── static/js/          # ai-stream.js
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           ├── lm.properties.example
│   │           ├── data/           # JSON seed data + runtime uploads
│   │           └── jsp/            # JSP view templates
│   └── test/java/                  # JUnit 5 tests
└── README.md

# Local build output (gitignored)
└── target/
```

---

## Prerequisites

Before building or running the project, ensure the following are installed:


| Tool          | Version     | Notes                                |
| ------------- | ----------- | ------------------------------------ |
| JDK           | 11 or above | Set `JAVA_HOME`                      |
| Apache Maven  | 3.9+        | `mvn` must be on your `PATH`         |


Install Maven on Windows (pick one):

```powershell
winget install Apache.Maven
```

Or download from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi), unzip, and add `bin` to `PATH`. Verify:

```powershell
mvn -version
```

---

## Build & Run Instructions

All commands run from the **repository root**.

### Build WAR

```powershell
mvn clean package
```

Output: `target/ta-recruitment.war`

### Run tests

```powershell
mvn test
```

### Run locally (embedded Tomcat, port 18080)

```powershell
mvn tomcat7:run-war
```

Open: [http://localhost:18080/ta-recruitment/login](http://localhost:18080/ta-recruitment/login)

Press `Ctrl+C` to stop.

### Deploy to an external Tomcat 9

1. `mvn clean package`
2. Copy `target/ta-recruitment.war` to `{TOMCAT}/webapps/`
3. Start Tomcat (`startup.bat` / IDE)
4. Open `http://localhost:8080/ta-recruitment/login` (port depends on your Tomcat)

---

## Applicant profile & CV

The `**/profile`** page (JSP: `profile.jsp`) is **English**. Applicants can:

- Enter **personal information**: full name, gender, degree, major, student ID, national ID, phone, email.
- Add **multiple education entries** (school, degree/level, major, period); rows are stored as JSON in `profiles.json` (`educationJson`).
- List **courses completed** (one course per line), **availability**, and **skills**.
- **Upload a CV** (multipart form, max ~10 MB); files are stored under `WEB-INF/data/cv/{userId}/` with the filename recorded on the profile.

On save, `**AuthService.updateUserBasics`** syncs **name**, **student ID**, and **email** to `users.json` (email must remain unique). `**CvDownloadServlet`** at `**/cv**` serves the current user’s uploaded file for preview/download.

---

## URL Routes


| URL                | Method     | Description                                                                               |
| ------------------ | ---------- | ----------------------------------------------------------------------------------------- |
| `/login`           | GET / POST | Login page                                                                                |
| `/register`        | GET / POST | New account registration                                                                  |
| `/logout`          | GET        | Invalidate session and redirect to login                                                  |
| `/profile`         | GET / POST | View and save applicant profile (multipart; CV upload)                                    |
| `/cv`              | GET        | Download / open uploaded CV (logged-in user only)                                         |
| `/forgot-password` | GET / POST | Forgot password (demo placeholder)                                                        |
| `/reset-password`  | GET / POST | Reset password (demo placeholder)                                                         |
| `/applications`    | GET        | View and filter own application statuses                                                  |
| `/job`             | GET        | TA job list + detail (`?id=`); login required; list page now includes TA-facing AI recommendation and missing-skills guidance |
| `/mo`              | GET / POST | Module organiser dashboard (`MoServlet`; mapping in `web.xml`)                            |
| `/mo/applicant-profile` | GET   | MO-only read-only applicant profile view (`?userId=`)                                     |
| `/admin`           | GET        | Administrator dashboard (requires `role` = `ADMIN`)                                       |
| `/admin/users`     | GET / POST | User management (ADMIN)                                                                   |
| `/admin/jobs`      | GET / POST | Job management (ADMIN)                                                                    |
| `/admin/job-view`  | GET        | Admin job view                                                                            |
| `/admin/ta-profiles` | GET      | TA profiles listing (ADMIN)                                                               |
| `/admin/cv`        | GET        | Admin CV download                                                                         |
| `/admin/export`    | GET        | Data export (ADMIN)                                                                       |
| `/admin/ai-demo`   | GET / POST | **Mock / demo** AI scaffold (admin only): skill match, missing skills, job recommendation |
| `/api/ai/stream`   | GET        | SSE endpoint for AI recommendation, skill-match, and missing-skills streaming responses   |


Seed accounts in `src/main/webapp/WEB-INF/data/users.json` (coursework; plain-text passwords): **admin** `admin@bupt.local` / `admin123` (`ADMIN`); **module organiser** `mo@bupt.local` / `mo123` (`MO`); demo TA applicants `alice.chen@bupt.local`, `brian.li@bupt.local`, `clara.wang@bupt.local`, and `daniel.zhang@bupt.local` all use password `ta123` and have profiles in `profiles.json`. New registrations get `role` = `TA`. Remove or change in production.

---

## Data Storage

All data is stored as JSON arrays in plain text files under `WEB-INF/data/`:


| File / location     | Contents                                                                                                                                                                                                                                                                                                                                                       |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `users.json`        | User accounts (`id`, `name`, `studentId`, `email`, `passwordHash`, `role`, `active`)                                                                                                                                                                                                                                                                           |
| `profiles.json`     | Applicant profiles per `userId`: personal fields (`fullName`, `gender`, `degree`, `major`, `studentId`, `idCard`, `phone`, `email`), `educationJson` (array of education objects), `courses` (multiline text), `freeTime`, `skills`, `cvFileName`. Legacy keys (`degreeProgramme`, `yearOfStudy`, `availability`, `selfIntro`) may still exist for older rows. |
| `applications.json` | Application records per user: `id`, `userId`, `moduleName`, `moduleCode`, `role`, `applicationDate`, `status` (Pending/Accepted/Rejected), `feedback`                                                                                                                                                                                                          |
| `jobs.json`         | Job postings (`Job` / `JobService`)                                                                                                                                                                                                                                                                                                                             |
| `cv/{userId}/…`     | Uploaded CV files (created at runtime; not checked into Git)                                                                                                                                                                                                                                                                                                   |


There is **no database**. The `FileStore` class reads and writes these files directly using a hand-rolled JSON parser (no external library). `FileStore.toJsonArrayOfObjects` serialises nested education data inside `educationJson`.

> **Security note (coursework prototype):** Passwords are currently stored in plain text. In a production system they must be hashed (e.g. with BCrypt).

---

## AI Integration / LM API Scaffold

This project includes a **small, pluggable LM (LLM) integration layer** for coursework and demos. It is **not** a production-grade AI product: there is no hard dependency on a specific vendor, and the default path is **fully offline**.

### What is implemented today

- **Framework only**: unified request/response types (`LmRequest` / `LmResponse`), configuration (`LmConfig`), factory (`LmClientFactory`), and **mock** + **HTTP placeholder** providers.

- **Default provider**: `mock` — deterministic, explainable outputs; **no API key** and **no outbound network** required.
- **HTTP provider**: `HttpLmClient` sends an **OpenAI Chat Completions–compatible** JSON body to `LM_BASE_URL` + `LM_HTTP_CHAT_PATH` and parses assistant text from the JSON response. Vendor-specific differences stay inside that class (marked with `TODO`).
- **Business entry point**: `AiFeatureService` + thin services (`SkillMatchService`, `MissingSkillService`, `RecommendationService`) assemble prompts; **servlets do not call the LM client directly**.
- **TA-facing UI**: `/job` exposes AI job recommendations for the current filtered list, plus missing-skills guidance for the selected job.
- **Admin demo UI**: `/admin/ai-demo` remains available as a clearly labelled **Mock / Demo** surface for manual prompt walkthroughs.

### Planned / supported scenarios (coursework)


| Scenario                     | Purpose                                      | Status                                            |
| ---------------------------- | -------------------------------------------- | ------------------------------------------------- |
| Skill matching               | Compare applicant skills vs job requirements | Mock + prompts ready; HTTP path uses same prompts |
| Missing skill identification | List gaps vs required skills                 | Mock + prompts ready                              |
| Job recommendation           | Rank or explain fit for open roles           | Mock + prompts ready                              |


Treat all model output as **assistive**: combine with module rules, interviews, and manual review.

### Configuration (priority order)

1. **Environment variables** (recommended for secrets)
2. **JVM system properties** (e.g. `-DLM_PROVIDER=mock`)
3. **Optional file**: copy `src/main/webapp/WEB-INF/lm.properties.example` to `WEB-INF/lm.properties` and edit (do **not** commit real keys)
4. **Built-in defaults**


| Key                 | Meaning                                    | Default                                                                     |
| ------------------- | ------------------------------------------ | --------------------------------------------------------------------------- |
| `LM_ENABLED`        | Master switch for AI features              | `true`                                                                      |
| `LM_PROVIDER`       | `mock` | `openai` | `custom`               | `mock`                                                                      |
| `LM_API_KEY`        | Bearer token for HTTP providers            | *(empty)*                                                                   |
| `LM_BASE_URL`       | API base, e.g. `https://api.openai.com/v1` | *(empty)*                                                                   |
| `LM_MODEL`          | Model name passed to the provider          | *(empty; mock uses `mock-model`, HTTP falls back to `gpt-4o-mini` in code)* |
| `LM_TIMEOUT_MS`     | HTTP timeout                               | `30000`                                                                     |
| `LM_HTTP_CHAT_PATH` | Path appended to base URL                  | `/chat/completions`                                                         |


**Safe degradation:** if `LM_PROVIDER` is `openai` or `custom` but `LM_BASE_URL` or `LM_API_KEY` is missing, the factory **falls back to `MockLmClient`** and logs a warning (no crash). If `LM_ENABLED=false`, the mock client returns a clear “AI disabled” message.

### Switching to a real API later

1. Set `LM_PROVIDER=openai` (or `custom` if you extend headers/body in `HttpLmClient`).
2. Set `LM_BASE_URL` and `LM_API_KEY` via environment or `lm.properties`.
3. Adjust `**HttpLmClient` only** for vendor JSON (Azure, Google, Anthropic, etc.): endpoint, headers, body shape, response parsing — keep servlets and `AiFeatureService` stable.

### New packages (for reports / code walkthrough)


| Location                 | Role                                                                            |
| ------------------------ | ------------------------------------------------------------------------------- |
| `com.bupt.ta.ai`         | `LmClient`, DTOs, `LmConfig`, `MockLmClient`, `HttpLmClient`, `LmClientFactory` |
| `com.bupt.ta.service.ai` | `AiFeatureService`, feature-specific prompt builders, `AiFeatureOutput`         |
| `com.bupt.ta.util`       | `AppConfig` (config merge), `HttpJsonClient` (JDK 11 `HttpClient`)              |


**No new runtime dependencies** for the WAR: networking uses the JDK’s `java.net.http` client. **Optional test-only** dependencies (JUnit 5, Mockito) are declared in `pom.xml` with `scope=test` for automated verification.

### Unit tests

Tests live under `src/test/java/**` (**237** cases, `mvn test`). Shared fixtures: `TestFixtures`, `ServletTestSupport`, `LmTestSupport`.

| Area | Test classes |
|------|----------------|
| **Model** | `JobApplicationStatsTest`, `TaWorkloadStatsTest`, `TaResumeDisplayTest`, `UserTest`, `ApplicationTest`, `MoWorkloadSnapshotTest` |
| **Services** | `AuthServiceTest`, `ProfileServiceTest`, `ApplicationServiceTest`, `JobServiceTest`, `FavoriteServiceTest`, `RecentlyViewedServiceTest`, `WorkloadServiceTest`, `FileStoreTest` |
| **Util** | `ApplicantFieldValidationTest`, `JobListFiltersTest`, `StringsTest`, `AppConfigTest`, `HttpJsonClientTest` |
| **AI / LM** | `LmProviderTypeTest`, `LmConfigTest`, `LmRequestTest`, `LmMessageTest`, `LmResponseTest`, `LmExceptionTest`, `MockLmClientTest`, `LmClientFactoryTest`, `HttpLmClientTest`, `SkillMatchServiceTest`, `MissingSkillServiceTest`, `RecommendationServiceTest`, `WorkloadAdviceServiceTest`, `AiFeatureServiceTest`, `AiFeatureOutputTest` |
| **Servlets** | `BaseServletTest`, `LoginServletTest`, `RegisterServletTest`, `LogoutServletTest`, `ApplicationServletTest`, `MoServletTest`, `AdminUserServletTest` |
| **Integration** | `UserLifecycleIntegrationTest`, `ApplicationFlowIntegrationTest` |

**Run JUnit tests:**

```powershell
mvn test
```

**Note:** `AppConfig.resolve` checks **environment variables first**. If a machine has `LM_PROVIDER` set globally, it may override a test file — unset it for deterministic tests, or rely on CI without those variables.

### API documentation (Javadoc)

All public types and methods under `src/main/java/com/bupt/ta/**` include English Javadoc (class purpose, parameters, return values, exceptions, and cross-links via `{@link}`).

**Generate HTML API docs:**

```powershell
mvn javadoc:javadoc
```

Open `target/reports/apidocs/index.html` in a browser. The `maven-javadoc-plugin` is configured in `pom.xml` (UTF-8, Java 11 source, `protected` visibility).

### Security and academic integrity

- **Never commit API keys**; use environment variables or local `lm.properties` excluded from Git.
- **AI output is advisory** only; hiring decisions must remain explainable and human-reviewed.
- For coursework, emphasise **transparent rules** (mock logic) and **where** a real model would plug in (`HttpLmClient` + factory).

---

## Team Members


| GitHub Alias  | Branch          | QMUL ID   | Name                  |
| ------------- | --------------- | --------- | --------------------- |
| Markrivera683 | Ruiyang_Sun     | 231226783 | Sun Ruiyang (孙瑞阳)     |
| christine288  | Qixin_Li        | 231225373 | Li Qixin (李其馨)        |
| Hzwnt         | Tianjing_Zhuang | 231225351 | Zhuang Tianjing (庄天婧) |
| S01ZZ         | Qinchun_Chen    | 231225410 | Chen Qinchun (陈沁纯)    |
| g726unknown   | Yifeng_Zhang    | 231226174 | Zhang Yifeng (张毅峰)    |
| negan525      | WeiJia_Xiao     | 231226233 | Xiao Weijia (肖炜佳)     |


**Note:**
Commits under the name "Chen Qinchun" correspond to the GitHub account **@S01ZZ**, due to local Git configuration (e.g., GitHub Desktop). All such contributions are made by the same contributor.

---

## Agile Development

The team follows an Agile workflow:

- **Product Backlog** maintained in `docs/ProductBacklog_group51.xlsx`
- Each team member works on a dedicated feature branch (named after the member)
- Pull Requests are used to merge completed features into `main`
- Iterations are tracked via GitHub Issues and the backlog spreadsheet

