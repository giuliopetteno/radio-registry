# Medical Devices Management System (Radio Registry)

Java backend microservice for managing medical imaging devices (CT scanners, X-ray systems, MRI, etc.) within healthcare organizations.

The system allows healthcare providers to register and manage medical devices, associate them with organizations, and organize them within specific departments.

It integrates with **Apache Kafka** to publish domain events for every relevant change, which are consumed by the [`radio-analytics`](https://github.com/giuliopetteno/radio-analytics) microservice to build and maintain a dedicated analytics read-model.

Application traces, metrics, and logs are exported via **OpenTelemetry** (OTLP) for consumption by the observability stack deployed in [`radio-infra`](https://github.com/giuliopetteno/radio-infra).

> **🚧 Work in Progress**
>
> This project is currently under active development and serves as a demonstration of modern Java backend development practices.
> New features, improvements, and additional integrations will be added over time.

## Live Demo

API Documentation (Swagger UI) is available at:
[radio-registry.giuliopetteno.dev](https://giuliopetteno.s.gy/radio-registry) *(short link for click tracking)*

> **Note:** Most endpoints require authentication and role-based authorization (**Operator, Technician, Admin**).
> 
> See the `/auth` endpoints in Swagger UI to register or log in. `/auth/login` returns a short-lived access token and a longer-lived refresh token. 
> 
> Use the `/auth/refresh` endpoint to obtain a new access token once it expires, and `/auth/logout` to revoke the refresh token.
> 
> New accounts register with **Operator** access by default, allowing read-only exploration of devices, device types, organizations, and departments.
> 
> **Technician** and **Admin** roles (full CRUD, user/role management, application health/info/metrics endpoints for operational monitoring) are assigned via a dedicated Admin-only endpoint and are not available for public self-registration.

## Features

- Medical devices lifecycle management across organizations and departments, with relational data persistence
- RESTful API architecture
- Authentication and role-based authorization (Operator, Technician, Admin)
- API documentation
- Event-driven architecture with Outbox Pattern
- Full audit trail of entity changes, including automatic versioning history
- DTO validation and exception handling
- Layered architecture following enterprise development practices
- Containerization
- Automated CI/CD pipeline
- Cloud deployment
- Full telemetry emission, designed for consumption by an external observability stack

## Technology Stack

- Java 25
- Spring Boot 4
- Spring Boot Actuator for health, info & metrics endpoints, enabling production monitoring
- Spring Security with JWT (refresh token rotation with reuse detection and cascading revocation for session security)
- Hibernate / JPA
- PostgreSQL
- Apache Kafka with Outbox Pattern for event-driven communication
- Audit logging via custom AOP aspects (action-level) and Hibernate Envers (entity-level versioning)
- Test suite: 
  - Unit tests (JUnit 5 & Mockito)
  - Slice tests (@WebMvcTest & @DataJpaTest)
  - Integration tests (@SpringBootTest & Testcontainers)
- Environment-based configuration for default & production profiles
- Containerization with Docker & Docker Compose
- Amazon Web Services (AWS) deployment:
  - EC2 (Docker Compose orchestration, IAM-only access via SSM)
  - ECR for container image registry
  - Automated CI/CD: GitHub Actions → OIDC → ECR → SSM Run Command deploy
  - Secrets management via AWS Systems Manager Parameter Store
- OpenTelemetry (OTLP) integration for distributed tracing, metrics, and structured logging
- Gradle build system with Kotlin DSL
- Swagger / OpenAPI for interactive API documentation & endpoint testing
- Lombok for boilerplate code reduction
