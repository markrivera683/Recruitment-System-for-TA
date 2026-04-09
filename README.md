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
| Build         | Manual compilation (`run.ps1`: one-command compile + deploy + start) |
| Dependencies  | `javax.servlet-api 4.0.1` (provided by Tomcat), `jstl 1.2`           |


---

## Repository Structure

```text
Recruitment-System-for-TA/
├── docs/                          # Project documents
│   ├── ProductBacklog_group51.xlsx
│   ├── Prototype_group51.pdf
│   └── Report_group51.pdf
├── src/                           # Source code root
│   ├── src/test/java/             # JUnit 5 tests (LM + AppConfig; optional Maven)
│   ├── src/main/
│   │   ├── java/com/bupt/ta/
│   │   │   ├── ai/                # LM client interfaces, mock + HTTP scaffold, factory
│   │   │   ├── model/             # Data models
│   │   │   │   ├── Roles.java     # Role constants (TA, ADMIN)
│   │   │   │   ├── User.java
│   │   │   │   ├── EducationEntry.java   # One row of education background
│   │   │   │   └── ApplicantProfile.java # Applicant profile + CV metadata
│   │   │   │   └── Application.java      # TA job application record
│   │   │   ├── service/           # Business logic & file persistence
│   │   │   │   ├── ai/            # AiFeatureService + feature prompt builders (mock/LM)
│   │   │   │   ├── FileStore.java       # Hand-rolled JSON read/write
│   │   │   │   ├── AuthService.java     # Register / Login / updateUserBasics
│   │   │   │   └── ProfileService.java  # Profile CRUD (incl. education JSON)
│   │   │   └── ApplicationService.java # Application CRUD + status update
│   │   │   ├── util/              # AppConfig, HttpJsonClient (JDK 11)
│   │   │   └── servlet/           # HTTP request handlers
│   │   │       ├── BaseServlet.java
│   │   │       ├── AiDemoServlet.java   # /admin/ai-demo (mock AI demo)
│   │   │       ├── AdminServlet.java
│   │   │       ├── LoginServlet.java
│   │   │       ├── RegisterServlet.java
│   │   │       ├── LogoutServlet.java
│   │   │       ├── ProfileServlet.java        # Multipart: profile + CV upload
│   │       ├── ApplicationServlet.java    # Application status + filter
│   │   │       ├── CvDownloadServlet.java # Serve uploaded CV (logged-in user)
│   │   │       ├── ForgotPasswordServlet.java
│   │   │       └── ResetPasswordServlet.java
│   │   └── webapp/
│   │       ├── static/css/app.css
│   │       ├── static/css/admin-dashboard.css # Admin UI
│   │       └── WEB-INF/
│   │           ├── lm.properties.example # Template for LM_* settings (copy to lm.properties)
│   │           ├── web.xml        # Servlet 4.0 deployment descriptor
│   │           ├── data/          # Runtime data files (JSON + uploaded CVs)
│   │           │   ├── users.json
│   │           │   ├── profiles.json
│   │           │   ├── applications.json
│   │           │   └── cv/{userId}/...    # Created at runtime when users upload CVs
│   │           └── jsp/           # JSP view templates
│   │               ├── admin/
│   │               │   ├── ai-demo.jsp
│   │               │   └── dashboard.jsp
│   │               ├── login.jsp
│   │               ├── register.jsp
│   │               ├── profile.jsp              # Profile + CV upload
│   │               ├── application-status.jsp   # My Applications page
│   │               ├── forgot-password.jsp
│   │               └── reset-password.jsp
│   ├── run.ps1                    # One-command: compile + package + deploy + start Tomcat
│   ├── run-tests.ps1              # Run JUnit tests (requires Maven: mvn test)
│   ├── compile.bat                # Legacy Windows build script
│   └── pom.xml                    # Maven reference (optional, IDE use only)
└── README.md
```

---

## Prerequisites

Before building or running the project, ensure the following are installed:


| Tool          | Version     | Notes                                             |
| ------------- | ----------- | ------------------------------------------------- |
| JDK           | 11 or above | `javac` and `jar` must be on your `PATH`          |
| Apache Tomcat | 9.x         | Provides `servlet-api.jar` and the runtime server |


---

## Build & Run Instructions

The recommended way is **one command**: `run.ps1` compiles all sources, packages the WAR, deploys to Tomcat, starts the server, and opens the browser automatically.

### Prerequisites


| Tool          | Version     | Notes                                      |
| ------------- | ----------- | ------------------------------------------ |
| JDK           | 11 or above | `javac` and `jar` must be accessible       |
| Apache Tomcat | 9.x         | Provides `servlet-api.jar` and the runtime |


Edit the two path variables at the top of `**src\run.ps1`** to match your machine:

```powershell
$JDK_HOME = 'D:\Apps\OpenJDKs\OpenJDK21.0.2'          # your JDK path
$TOMCAT   = 'D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115' # your Tomcat path
```

### Run (one command)

Open PowerShell and run:

```powershell
& 'C:\path\to\Recruitment-System-for-TA\src\run.ps1'
```

Or from inside the `src\` folder:

```powershell
.\run.ps1
```

The script will:

1. **Clean** the previous build output
2. **Compile** all 14 Java source files with `javac -source 11`
3. **Package** `webapp/` + compiled classes into `ta-recruitment.war`
4. **Deploy** the WAR to `%TOMCAT%\webapps\`
5. **Start** Tomcat (`startup.bat` with `CATALINA_HOME` set automatically)
6. **Open** `http://localhost:8080/ta-recruitment/login` in the default browser

### Stop Tomcat

```powershell
Stop-Process -Name java -Force
```

### Source files compiled

All files in a single `javac` pass (order matters for dependencies):

```
model/Roles.java
model/EducationEntry.java
model/User.java
model/ApplicantProfile.java
service/FileStore.java
service/AuthService.java
service/ProfileService.java
servlet/BaseServlet.java
servlet/LoginServlet.java
servlet/RegisterServlet.java
servlet/LogoutServlet.java
servlet/ProfileServlet.java
servlet/ForgotPasswordServlet.java
servlet/ResetPasswordServlet.java
```

> **Note:** The warning `bootstrap classpath not set with -source 11` appears when compiling with JDK 17+ targeting Java 11. It is harmless and does not affect the build.

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
| `/admin`           | GET        | Administrator dashboard (requires `role` = `ADMIN`)                                       |
| `/admin/ai-demo`   | GET / POST | **Mock / demo** AI scaffold (admin only): skill match, missing skills, job recommendation |


The repository includes a **development seed** administrator in `src/src/main/webapp/WEB-INF/data/users.json`: email `**admin@bupt.local`**, password `**admin123**`. Change or remove this account in production; new registrations get `role` = `TA`.

---

## Data Storage

All data is stored as JSON arrays in plain text files under `WEB-INF/data/`:


| File / location     | Contents                                                                                                                                                                                                                                                                                                                                                       |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `users.json`        | Registered user accounts (`id`, `name`, `studentId`, `email`, `passwordHash`, `role`)                                                                                                                                                                                                                                                                          |
| `profiles.json`     | Applicant profiles per `userId`: personal fields (`fullName`, `gender`, `degree`, `major`, `studentId`, `idCard`, `phone`, `email`), `educationJson` (array of education objects), `courses` (multiline text), `freeTime`, `skills`, `cvFileName`. Legacy keys (`degreeProgramme`, `yearOfStudy`, `availability`, `selfIntro`) may still exist for older rows. |
| `applications.json` | Application records per user: `id`, `userId`, `moduleName`, `moduleCode`, `role`, `applicationDate`, `status` (Pending/Accepted/Rejected), `feedback`                                                                                                                                                                                                          |
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
- **Demo UI**: `/admin/ai-demo` (admin login required) — clearly labelled **Mock / Demo**.

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
3. **Optional file**: copy `src/src/main/webapp/WEB-INF/lm.properties.example` to `WEB-INF/lm.properties` and edit (do **not** commit real keys)
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

### Unit tests (LM workflow)

Tests live under `src/src/test/java/**` and mirror the LM integration layers:

| Test class | What it verifies |
|---|---|
| `LmProviderTypeTest` | `LmProviderType.fromString` |
| `AppConfigTest` | `AppConfig.resolve` (system property vs file vs default) |
| `LmConfigTest` | `LmConfig.load` + `mock.properties` / system overrides |
| `LmRequestTest` | `LmRequest` builder and immutable message list |
| `MockLmClientTest` | `MockLmClient` per `AiFeatureNames` |
| `LmClientFactoryTest` | `LmClientFactory.create` (mock, HTTP, fallback) |
| `HttpLmClientTest` | `HttpLmClient` fails fast without credentials |
| `SkillMatchServiceTest` / `MissingSkillServiceTest` / `RecommendationServiceTest` | Each feature service + `LmClient` |
| `AiFeatureServiceTest` | Full pipeline: config → factory → `AiFeatureService` |
| `AiFeatureOutputTest` | `AiFeatureOutput.fromResponse` mapping |

`com.bupt.ta.testsupport.LmTestSupport` provides a mocked `ServletContext` with optional `WEB-INF/lm.properties` content.

**Run (requires Maven on `PATH`):**

```powershell
cd src
mvn test
```

Or use `src/run-tests.ps1` (same command, with a clear error if `mvn` is missing).

**Note:** `AppConfig.resolve` checks **environment variables first**. If a machine has `LM_PROVIDER` set globally, it may override a test file — unset it for deterministic tests, or rely on CI without those variables.

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

