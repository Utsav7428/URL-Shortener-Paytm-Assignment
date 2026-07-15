# URL Shortener with Analytics Dashboard

A production-style URL Shortener built using **Spring Boot** and **PostgreSQL** that supports custom aliases, Base62 short code generation, asynchronous click analytics, and a responsive HTML dashboard.

## Features

-  Shorten long URLs
-  Custom aliases
-  Collision-free Base62 encoded short codes
-  HTTP 302 redirects
-  Click analytics dashboard
-  Asynchronous analytics collection using Spring `@Async`
-  PostgreSQL persistence
-  Flyway database migrations
-  Unit and Integration Tests

---

# Tech Stack

| Layer | Technology |
|--------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Migration | Flyway |
| Build Tool | Maven |
| Frontend | HTML, CSS, JavaScript |
| Async Processing | Spring `@Async` |

---

# High Level Architecture

The application separates the user redirect path from analytics collection to minimize redirect latency.

<img width="495" height="866" alt="Screenshot (195)" src="https://github.com/user-attachments/assets/942badfc-d3ce-44c7-b4ac-e568eaeba22a" />

<img width="1127" height="724" alt="Screenshot (196)" src="https://github.com/user-attachments/assets/76d3738b-8f27-4717-8fe7-fc0a022d0f2e" />



---

# Design Decisions

## Collision-Free Short Code Generation

Instead of generating random hashes, every URL receives a PostgreSQL sequence ID.

The numeric identifier is converted into a compact Base62 string.

Advantages:

- Guaranteed uniqueness
- No collision checks
- No retry loops
- Small URL length

---

## Asynchronous Analytics Logging

Analytics persistence is completely decoupled from the redirect flow.

Workflow:

1. Lookup short code.
2. Return HTTP 302 immediately.
3. Record click analytics in a background thread.

This ensures database writes never increase redirect latency.

---

## Regex Route Mapping

The redirect endpoint uses:

```java
/{shortCode:[a-zA-Z0-9\\-_]+}
```

This prevents requests such as

```
/index.html
/style.css
/app.js
/favicon.ico
```

from being intercepted by the redirect controller.

---

# Database Schema

Database migrations are managed using Flyway.

## url_mappings

| Column | Type | Constraints |
|---------|------|------------|
| id | BIGSERIAL | Primary Key |
| original_url | TEXT | NOT NULL |
| short_code | VARCHAR(50) | UNIQUE |
| is_custom | BOOLEAN | DEFAULT FALSE |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL |

### Indexes

- PRIMARY KEY(id)
- UNIQUE(short_code)

---

## click_analytics

| Column | Type | Constraints |
|---------|------|------------|
| id | BIGSERIAL | Primary Key |
| url_mapping_id | BIGINT | Foreign Key |
| clicked_at | TIMESTAMP WITH TIME ZONE | NOT NULL |
| user_agent | TEXT | |
| referer | TEXT | |
| ip_address | VARCHAR(45) | Supports IPv4 & IPv6 |

### Indexes

- PRIMARY KEY(id)
- INDEX(url_mapping_id)

---

# REST API

## Create Short URL

**POST**

```
/api/v1/shorten
```

### Request

```json
{
  "url": "https://github.com/Utsav7428",
  "customAlias": "github"
}
```

### Response

```json
{
  "originalUrl": "https://github.com/Utsav7428",
  "shortUrl": "http://localhost:8080/github",
  "shortCode": "github",
  "createdAt": "2026-07-15T15:49:25Z",
  "custom": true
}
```

---

## Fetch All Links

**GET**

```
/api/v1/links
```

Response

```json
[
  {
    "originalUrl": "https://github.com/Utsav7428",
    "shortUrl": "http://localhost:8080/github",
    "shortCode": "github",
    "createdAt": "2026-07-15T15:49:25Z",
    "custom": true
  }
]
```

---

## Fetch Analytics

**GET**

```
/api/v1/analytics/{shortCode}
```

Response

```json
{
  "shortCode": "github",
  "originalUrl": "https://github.com/Utsav7428",
  "totalClicks": 42,
  "recentClicks": [
    {
      "ipAddress": "127.0.0.1",
      "userAgent": "Mozilla/5.0...",
      "referer": "Direct",
      "clickedAt": "2026-07-15T15:50:02Z"
    }
  ]
}
```

---

# Running the Application

## Prerequisites

- Java 17
- Docker
- Maven

---

## Start PostgreSQL

```bash
docker run \
--name url-shortener-db \
-e POSTGRES_DB=url_shortener \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-p 5432:5432 \
-d postgres:15-alpine
```

---

## Build

```bash
./mvnw clean compile
```

---

## Run

```bash
./mvnw spring-boot:run
```

Open

```
http://localhost:8080
```

---

# Testing

Run all tests

```bash
./mvnw test
```

Tests include:

- Base62 Encoder
- Controller Integration Tests
- Redirect Tests
- Validation Tests

---

# Future Improvements

- Redis cache for hot URLs
- Rate limiting
- URL expiration
- QR code generation
- User authentication
- Geo-location analytics
- Prometheus metrics
- Docker Compose deployment

---

# Author

**Utsav Agarwal**

Backend Engineer

GitHub: https://github.com/Utsav7428

---

# License

MIT License
