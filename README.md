# URL Shortener & Link Analytics Service

Hello! Thank you for reviewing this submission. This service is designed as a production-ready microservice built using Spring Boot, PostgreSQL, and Flyway. 

It handles the transformation of long URLs into short, deterministic, collision-free aliases, manages custom short codes, and tracks incoming traffic via HTTP 301 permanent redirects.

---

## Core Features & Implementation Architecture

* **Collision-Free Design:** Utilizes a Base62 encoding strategy mapped against an auto-incrementing database primary key sequence. This completely removes application-level collision risks and ensures $O(1)$ lookups.
* **Duplicate URL Strategy:** Handled intentionally at the database level using unique constraints. Resubmitting an identical long URL fetches the existing short code to preserve key space, unless a unique custom alias is explicitly provided.
* **Database Schema Migrations:** Managed exclusively via **Flyway** (`ddl-auto: validate`). Hibernate is restricted to object-relational mapping, ensuring all DDL scripts are tracked, version-controlled, and auditable.
* **Input Sanitization:** Uses strict URL format validation using standard web protocols (`http://` or `https://`) to prevent malformed injections.

---

##  Tech Stack & Requirements
* **Java:** Version 17
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL 15
* **Migrations:** Flyway
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito, and MockMvc

---

## ⚙️ Getting Started

### 1. Spin Up the Database
Ensure Docker Desktop is running, then execute the following command from the project root:
```bash
docker-compose up -d
