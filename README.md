# Recruitment System for TA

This repository contains the group project for **EBU6304 Software Engineering**.
The project aims to develop a **Teaching Assistant Recruitment System** for BUPT International School to improve the efficiency of the TA recruitment process.

---

## Project Overview

The current TA recruitment process mainly relies on email and Excel files, which may lead to fragmented information, repeated manual work, and difficulty in tracking applications. This project proposes a lightweight recruitment system that allows:

- **TA Applicants** to register, create profiles, upload CVs, browse job postings, and submit applications online
- **Module Organisers** to post TA jobs and review incoming applications
- **Administrators** to monitor overall recruitment information and workload

The system is developed following **Agile software development methods**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Web Framework | Java Servlet 4.0 / JSP 2.3 |
| Server | Apache Tomcat 9.x |
| Data Storage | Plain JSON files (`.json`) — no database |
| Build | Manual compilation (`compile.bat` / `build.ps1`: `javac` + `jar`) |
| Dependencies | `javax.servlet-api 4.0.1` (provided by Tomcat), `jstl 1.2` |

---

## Repository Structure

```text
Recruitment-System-for-TA/
├── docs/                          # Project documents
│   ├── ProductBacklog_group51.xlsx
│   ├── Prototype_group51.pdf
│   └── Report_group51.pdf
├── src/                           # Source code root
│   ├── src/main/
│   │   ├── java/com/bupt/ta/
│   │   │   ├── model/             # Data models
│   │   │   │   ├── Roles.java     # Role constants (TA, ADMIN)
│   │   │   │   ├── User.java
│   │   │   │   ├── EducationEntry.java   # One row of education background
│   │   │   │   └── ApplicantProfile.java # Applicant profile + CV metadata
│   │   │   ├── service/           # Business logic & file persistence
│   │   │   │   ├── FileStore.java       # Hand-rolled JSON read/write
│   │   │   │   ├── AuthService.java     # Register / Login / updateUserBasics
│   │   │   │   └── ProfileService.java  # Profile CRUD (incl. education JSON)
│   │   │   └── servlet/           # HTTP request handlers
│   │   │       ├── BaseServlet.java
│   │   │       ├── AdminServlet.java
│   │   │       ├── LoginServlet.java
│   │   │       ├── RegisterServlet.java
│   │   │       ├── LogoutServlet.java
│   │   │       ├── ProfileServlet.java    # Multipart: profile + CV upload
│   │   │       ├── CvDownloadServlet.java # Serve uploaded CV (logged-in user)
│   │   │       ├── ForgotPasswordServlet.java
│   │   │       └── ResetPasswordServlet.java
│   │   └── webapp/
│   │       ├── static/css/app.css
│   │       ├── static/css/admin-dashboard.css # Admin UI
│   │       └── WEB-INF/
│   │           ├── web.xml        # Servlet 4.0 deployment descriptor
│   │           ├── data/          # Runtime data files (JSON + uploaded CVs)
│   │           │   ├── users.json
│   │           │   ├── profiles.json
│   │           │   └── cv/{userId}/...    # Created at runtime when users upload CVs
│   │           └── jsp/           # JSP view templates
│   │               ├── admin/
│   │               │   └── dashboard.jsp
│   │               ├── login.jsp
│   │               ├── register.jsp
│   │               ├── profile.jsp        # English UI: profile + CV upload
│   │               ├── forgot-password.jsp
│   │               └── reset-password.jsp
│   ├── compile.bat                # Recommended Windows build (one javac pass, all .java)
│   ├── build.ps1                  # PowerShell build (edit JDK/Tomcat paths)
│   └── pom.xml                    # Maven reference (optional, IDE use only)
└── README.md
```

---

## Prerequisites

Before building or running the project, ensure the following are installed:

| Tool | Version | Notes |
|---|---|---|
| JDK | 11 or above | `javac` and `jar` must be on your `PATH` |
| Apache Tomcat | 9.x | Provides `servlet-api.jar` and the runtime server |

---

## Build Instructions (Manual Compilation)

All commands below are run from the **`src\`** directory. You need **JDK 11+** (`javac`, `jar` on `PATH`) and **Apache Tomcat 9.x** (for `lib\servlet-api.jar` at compile time and for deployment).

### Option A — `compile.bat` (recommended on Windows)

1. Edit **`src\compile.bat`** and set `TOMCAT_HOME` to your Tomcat install folder (must contain `lib\servlet-api.jar`).
2. From a terminal:

```bat
cd src
compile.bat
```

The script **cleans**, runs **one** `javac --release 11` pass over **all** `.java` files (including `Roles`, `EducationEntry`, `ProfileServlet`, `CvDownloadServlet`, etc.), copies `webapp` into `out\`, seeds empty JSON if needed, and produces **`ta-recruitment.war`** in `src\`.

3. Copy `ta-recruitment.war` to `%TOMCAT_HOME%\webapps\` (remove an older exploded `webapps\ta-recruitment` folder if Tomcat already deployed a previous version).
4. Start Tomcat (`bin\startup.bat`) and open **http://localhost:8080/ta-recruitment/login**.

> **Important:** Do not split compilation across multiple `javac` runs with different file lists — the project will fail to compile or the WAR will miss classes. Keep a single pass listing every source file (as in `compile.bat`).

### Option B — `build.ps1`

Edit the JDK and Tomcat paths at the top of **`src\build.ps1`**, then from `src\`:

```powershell
.\build.ps1
```

It performs the same steps as `compile.bat` (including `EducationEntry.java` and `CvDownloadServlet.java`).

---

## Applicant profile & CV

The **`/profile`** page (JSP: `profile.jsp`) is **English**. Applicants can:

- Enter **personal information**: full name, gender, degree, major, student ID, national ID, phone, email.
- Add **multiple education entries** (school, degree/level, major, period); rows are stored as JSON in `profiles.json` (`educationJson`).
- List **courses completed** (one course per line), **availability**, and **skills**.
- **Upload a CV** (multipart form, max ~10 MB); files are stored under `WEB-INF/data/cv/{userId}/` with the filename recorded on the profile.

On save, **`AuthService.updateUserBasics`** syncs **name**, **student ID**, and **email** to `users.json` (email must remain unique). **`CvDownloadServlet`** at **`/cv`** serves the current user’s uploaded file for preview/download.

---

## URL Routes

| URL | Method | Description |
|---|---|---|
| `/login` | GET / POST | Login page |
| `/register` | GET / POST | New account registration |
| `/logout` | GET | Invalidate session and redirect to login |
| `/profile` | GET / POST | View and save applicant profile (multipart; CV upload) |
| `/cv` | GET | Download / open uploaded CV (logged-in user only) |
| `/forgot-password` | GET / POST | Forgot password (demo placeholder) |
| `/reset-password` | GET / POST | Reset password (demo placeholder) |
| `/admin` | GET | Administrator dashboard (requires `role` = `ADMIN`) |

The repository includes a **development seed** administrator in `src/src/main/webapp/WEB-INF/data/users.json`: email **`admin@bupt.local`**, password **`admin123`**. Change or remove this account in production; new registrations get `role` = `TA`.

---

## Data Storage

All data is stored as JSON arrays in plain text files under `WEB-INF/data/`:

| File / location | Contents |
|---|---|
| `users.json` | Registered user accounts (`id`, `name`, `studentId`, `email`, `passwordHash`, `role`) |
| `profiles.json` | Applicant profiles per `userId`: personal fields (`fullName`, `gender`, `degree`, `major`, `studentId`, `idCard`, `phone`, `email`), `educationJson` (array of education objects), `courses` (multiline text), `freeTime`, `skills`, `cvFileName`. Legacy keys (`degreeProgramme`, `yearOfStudy`, `availability`, `selfIntro`) may still exist for older rows. |
| `cv/{userId}/…` | Uploaded CV files (created at runtime; not checked into Git) |

There is **no database**. The `FileStore` class reads and writes these files directly using a hand-rolled JSON parser (no external library). `FileStore.toJsonArrayOfObjects` serialises nested education data inside `educationJson`.

> **Security note (coursework prototype):** Passwords are currently stored in plain text. In a production system they must be hashed (e.g. with BCrypt).

---

## Team Members

| GitHub Alias | Branch | QMUL ID | Name |
|---|---|---|---|
| Markrivera683 | Ruiyang_Sun | 231226783 | Sun Ruiyang (孙瑞阳) |
| christine288 | Qixin_Li | 231225373 | Li Qixin (李其馨) |
| Hzwnt | Tianjing_Zhuang | 231225351 | Zhuang Tianjing (庄天婧) |
| S01ZZ | Qinchun_Chen | 231225410 | Chen Qinchun (陈沁纯) |
| g726unknown | Yifeng_Zhang | 231226174 | Zhang Yifeng (张毅峰) |
| negan525 | WeiJia_Xiao | 231226233 | Xiao Weijia (肖炜佳) |

**Note:**
Commits under the name "Chen Qinchun" correspond to the GitHub account **@S01ZZ**, due to local Git configuration (e.g., GitHub Desktop). All such contributions are made by the same contributor.

---

## Agile Development

The team follows an Agile workflow:

- **Product Backlog** maintained in `docs/ProductBacklog_group51.xlsx`
- Each team member works on a dedicated feature branch (named after the member)
- Pull Requests are used to merge completed features into `main`
- Iterations are tracked via GitHub Issues and the backlog spreadsheet