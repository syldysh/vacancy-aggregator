<div align="center">

# Vacancy Aggregator

### A web application for collecting, storing, and exploring job vacancies

[Русская версия](README_RU.md)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=flat-square&logo=gradle&logoColor=white)

</div>

## About the project

This is my personal side project for working with job vacancies in one place. I started it because a job search often means opening many pages and using the same filters again and again. I wanted a small service where I could load vacancies, narrow down the results, and compare them on one screen.

The application receives data from the open API of [Trudvsem](https://trudvsem.ru/), saves it in PostgreSQL, and shows it in a simple web interface. A user can search the collected vacancies, combine several filters, compare salaries, and open the original vacancy page.

## How it works

```text
Trudvsem API  →  Spring Boot service  →  PostgreSQL  →  REST API  →  Web interface
```

When the application starts, it requests the first page of vacancies from the source. More pages can be loaded from the interface. Before saving a vacancy, the application checks its link and source to avoid duplicates.

## Main features

- Loading vacancies from the Trudvsem public API
- Choosing how many pages to load, from 1 to 100
- Automatic import of the first page at application startup
- Search by vacancy title, description, and requirements
- Filtering by city, company, and salary range
- Sorting by publication date or salary
- A detailed view with description, requirements, and a link to the original vacancy
- Statistics for the current result: vacancies, cities, and companies
- Persistent storage in PostgreSQL
- Database schema management with Liquibase
- REST endpoints for vacancies, filters, and data import
- Automated tests for controllers, services, repositories, and the data model

## Technology

| Part | Tools |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA |
| Database | PostgreSQL, Liquibase |
| Frontend | HTML, CSS, JavaScript, Thymeleaf, Bootstrap 5 |
| Build and tests | Gradle Wrapper, JUnit, Spring Boot Test, H2 |
| Local environment | Docker Compose |

## Run locally

### Requirements

- Java 21 or newer
- Docker with Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/syldysh/vacancy-aggregator.git
cd vacancy-aggregator/project
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

The included Compose file starts PostgreSQL on port `5432`. The database settings already match `application.properties`, so no extra configuration is needed for a local start.

### 3. Start the application

On macOS or Linux:

```bash
sh gradlew bootRun
```

On Windows:

```powershell
gradlew.bat bootRun
```

Open [http://localhost:8080](http://localhost:8080) in a browser. The first data import may take a short time.

### 4. Stop the database

```bash
docker compose down
```

## Run the tests

From the `project` directory:

```bash
sh gradlew test
```

The test environment uses an in-memory H2 database, so PostgreSQL is not required for this command.

## REST API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/vacancies` | Return all saved vacancies |
| `GET` | `/api/vacancies/{id}` | Return one vacancy by ID |
| `GET` | `/api/vacancies/search` | Search and filter vacancies |
| `GET` | `/api/cities` | Return available cities |
| `GET` | `/api/companies` | Return available companies |
| `POST` | `/api/parse-vacancies?pages=1` | Load new vacancies from the source |

The search endpoint accepts optional `city`, `company`, `keyword`, `salaryFrom`, and `salaryTo` parameters.

## Project structure

```text
project/
├── src/main/java/ru/goyda/hse/
│   ├── config/       # Application setup and startup import
│   ├── controller/   # Web and REST controllers
│   ├── dto/          # External API response models
│   ├── model/        # Database entities
│   ├── repository/   # Data access and search queries
│   └── service/      # Import and application logic
├── src/main/resources/
│   ├── db/           # Liquibase migrations
│   ├── static/       # CSS and JavaScript
│   └── templates/    # Thymeleaf page
├── src/test/         # Automated tests
├── build.gradle
└── compose.yaml
```

## What I built

I built the project as a small full-stack service. Spring Boot handles requests to the external API and the application logic, PostgreSQL stores the vacancies, and Liquibase keeps the database structure consistent. I separated the backend into controller, service, and repository layers and added tests for the main parts of the application.

The vacancy data belongs to its original providers and is accessed through the public Trudvsem API.
