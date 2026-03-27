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
| Build | Manual compilation (`javac` + `jar`) |
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
│   │   │   │   ├── User.java
│   │   │   │   └── ApplicantProfile.java
│   │   │   ├── service/           # Business logic & file persistence
│   │   │   │   ├── FileStore.java       # Hand-rolled JSON read/write
│   │   │   │   ├── AuthService.java     # Register / Login
│   │   │   │   └── ProfileService.java  # Profile CRUD
│   │   │   └── servlet/           # HTTP request handlers
│   │   │       ├── BaseServlet.java
│   │   │       ├── LoginServlet.java
│   │   │       ├── RegisterServlet.java
│   │   │       ├── LogoutServlet.java
│   │   │       ├── ProfileServlet.java
│   │   │       ├── ForgotPasswordServlet.java
│   │   │       └── ResetPasswordServlet.java
│   │   └── webapp/
│   │       ├── static/css/app.css # Stylesheet
│   │       └── WEB-INF/
│   │           ├── web.xml        # Servlet 4.0 deployment descriptor
│   │           ├── data/          # Runtime data files (JSON)
│   │           │   ├── users.json
│   │           │   └── profiles.json
│   │           └── jsp/           # JSP view templates
│   │               ├── login.jsp
│   │               ├── register.jsp
│   │               ├── profile.jsp
│   │               ├── forgot-password.jsp
│   │               └── reset-password.jsp
│   ├── build.ps1                  # PowerShell build script (Windows)
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

All commands below are run from the **`src\`** directory.

### Step 1 — Compile Java sources

Open PowerShell in the `src\` folder and run:

```powershell
$J  = "D:\Apps\OpenJDKs\OpenJDK21.0.2\bin"
$S  = "D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115\lib\servlet-api.jar"
$SRC = "src\main\java"
$OUT = "out\WEB-INF\classes"
New-Item -Force -ItemType Directory $OUT | Out-Null
& "$J\javac.exe" -source 11 -target 11 -encoding UTF-8 -cp $S -d $OUT `
  $SRC\com\bupt\ta\model\User.java `
  $SRC\com\bupt\ta\model\ApplicantProfile.java `
  $SRC\com\bupt\ta\service\FileStore.java `
  $SRC\com\bupt\ta\service\AuthService.java `
  $SRC\com\bupt\ta\service\ProfileService.java `
  $SRC\com\bupt\ta\servlet\BaseServlet.java `
  $SRC\com\bupt\ta\servlet\LoginServlet.java `
  $SRC\com\bupt\ta\servlet\RegisterServlet.java `
  $SRC\com\bupt\ta\servlet\LogoutServlet.java `
  $SRC\com\bupt\ta\servlet\ProfileServlet.java `
  $SRC\com\bupt\ta\servlet\ForgotPasswordServlet.java `
  $SRC\com\bupt\ta\servlet\ResetPasswordServlet.java
```

> Adjust `$J` and `$S` to match your local JDK and Tomcat installation paths.

### Step 2 — Package as WAR

```powershell
$T = "D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115"
Copy-Item -Recurse -Force src\main\webapp\* out\
Push-Location out
& "D:\Apps\OpenJDKs\OpenJDK21.0.2\bin\jar.exe" -cvf .\..\ta-recruitment.war .
Pop-Location
```

### Step 3 — Deploy to Tomcat

```powershell
Copy-Item -Force ta-recruitment.war "$T\webapps\"
```

### Step 4 — Start Tomcat

```powershell
& "$T\bin\startup.bat"
```

### Step 5 — Open in browser

```
http://localhost:8080/ta-recruitment/login
```

---

## URL Routes

| URL | Method | Description |
|---|---|---|
| `/login` | GET / POST | Login page |
| `/register` | GET / POST | New account registration |
| `/logout` | GET | Invalidate session and redirect to login |
| `/profile` | GET / POST | View and save applicant profile |
| `/forgot-password` | GET / POST | Forgot password (demo placeholder) |
| `/reset-password` | GET / POST | Reset password (demo placeholder) |

---

## Data Storage

All data is stored as JSON arrays in plain text files under `WEB-INF/data/`:

| File | Contents |
|---|---|
| `users.json` | Registered user accounts (`id`, `name`, `studentId`, `email`, `passwordHash`, `role`) |
| `profiles.json` | Applicant profiles (`userId`, `degreeProgramme`, `yearOfStudy`, `skills`, `availability`, `selfIntro`, `cvFileName`) |

There is **no database**. The `FileStore` class reads and writes these files directly using a hand-rolled JSON parser (no external library).

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

---

## Agile Development

The team follows an Agile workflow:

- **Product Backlog** maintained in `docs/ProductBacklog_group51.xlsx`
- Each team member works on a dedicated feature branch (named after the member)
- Pull Requests are used to merge completed features into `main`
- Iterations are tracked via GitHub Issues and the backlog spreadsheet