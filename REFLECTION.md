## 1. What did I ask the AI to do, and what did I decide myself?

I treated the AI as a development companion to discuss ideas, review code snippets, generate the UML diagrams, and help structure the project documentation. It was also useful for brainstorming test cases and improving the README.

Initially, the AI suggested building the project using Python (FastAPI) or Node.js (Express) because they are lightweight and quick to set up for a small assignment. However, I decided to use Java and Spring Boot instead. Java is the language I am most comfortable with, and I felt a strongly typed framework would result in a cleaner and more maintainable backend.

While the AI assisted with boilerplate code and implementation suggestions, the overall architecture and core implementation were my own decisions. In particular, I:

* Chose Java and Spring Boot as the technology stack.
* Designed the relational database schema.
* Created the Flyway migration scripts.
* Implemented Base62 short-code generation using a PostgreSQL sequence.
* Designed the REST API endpoints and validation logic.
* Configured asynchronous analytics logging using Spring's `@Async` support.
* Built the frontend dashboard and integrated it with the backend APIs.

---

## 2. Where did I disagree with the AI?

### Database Schema Management

The AI suggested relying on Hibernate's automatic schema generation (`ddl-auto=create` or `ddl-auto=update`) to manage the database structure. I decided against this because automatic schema updates can introduce unexpected changes and are generally not recommended beyond development environments. Instead, I disabled Hibernate DDL generation and used Flyway with version-controlled SQL migration scripts to keep schema evolution explicit and reproducible.

### Short Code Generation

The first recommendation was to generate random alphanumeric strings and retry until a unique value was found. Although this approach works, it requires collision checks and additional database lookups. I instead generated a unique numeric ID using a PostgreSQL sequence and encoded it into a Base62 string. This guarantees uniqueness without retries while keeping the generated URLs compact.

### Code Structure

Some of the generated suggestions introduced additional abstraction layers and interfaces that were unnecessary for a project of this size. I preferred a simpler, modular design that follows standard Spring Boot conventions without introducing avoidable complexity.

---

## 3. The biggest trade-offs I made

### Fast Redirects vs. Immediate Analytics

The primary responsibility of a URL shortener is to redirect users with minimal latency. To achieve this, I chose to process click analytics asynchronously using Spring's `@Async` support. The redirect response is returned immediately after resolving the destination URL, while analytics are written in a background thread. The trade-off is eventual consistency—the analytics dashboard may take a short time to reflect new clicks—but redirect performance remains unaffected.

### Sequential IDs vs. Link Obfuscation

Using sequential database IDs encoded as Base62 provides deterministic, collision-free short code generation with a very simple implementation. The downside is that generated URLs follow a predictable order, making them easier to enumerate. For this assignment, I considered the simplicity and reliability of this approach to outweigh the need for stronger obfuscation.

### Simplicity vs. Scalability

The application could have incorporated Redis caching, Kafka-based event streaming, or a microservice architecture. Instead, I intentionally kept it as a modular Spring Boot application backed by PostgreSQL. This makes the project easier to understand, test, and run locally while maintaining clear separation between controllers, services, repositories, and asynchronous processing. The design also leaves room for future scaling if required.

---

## 4. What would I improve with another day?

With another day to continue development, I would focus on production-readiness rather than adding completely new functionality.

The improvements I would prioritize are:

* Introduce Redis caching for frequently accessed URL mappings.
* Implement rate limiting to prevent abuse of the shortening and redirect endpoints.
* Provide a `docker-compose.yml` file so the application and PostgreSQL database can be started with a single command.
* Support URL expiration with scheduled cleanup of expired links and their associated analytics.
* Expand the automated test suite to cover concurrent requests, transaction boundaries, and additional failure scenarios.

Overall, I found AI to be most useful as a tool for brainstorming, reviewing ideas, and improving documentation rather than generating complete solutions. I used its suggestions selectively, making the final architectural and implementation decisions based on simplicity, maintainability, and the goals of the assignment.
