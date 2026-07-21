# Medical Devices Management System (Radio Registry)

A modern backend application for managing medical imaging devices (CT scanners, X-ray systems, MRI, etc.) within healthcare organizations.

The system allows healthcare providers to register and manage medical devices, associate them with organizations, and organize them within specific departments.

It integrates with Apache Kafka to publish domain events for every relevant change, which are consumed by the [`radio-analytics`](https://github.com/giuliopetteno/radio-analytics) microservice to build and maintain a dedicated analytics read-model.

> **⚠️ Work in Progress**
>
> This project is currently under active development and serves as a demonstration of modern Java backend development practices.
> New features, improvements, and additional integrations will be added over time.

## Live Demo

API Documentation (Swagger UI) is available at:
[radio-registry.giuliopetteno.dev](https://giuliopetteno.s.gy/radio-registry)

> **Note:** Most endpoints require authentication and role-based access control (**Operator, Technician, Admin**).
> 
> See the `/auth` endpoints in Swagger UI to register or log in.
> 
> New accounts register with **Operator** access by default, allowing read-only exploration of devices, device types, organizations, and departments.
> 
> **Technician** and **Admin** roles (full CRUD, user/role management, application health/info/metrics endpoints for operational monitoring) are assigned via a dedicated Admin-only endpoint and are not available for public self-registration.

## Features

- Medical devices lifecycle management across organizations and departments, with relational data persistence
- RESTful API architecture
- Authentication and authorization with Spring Security
- API documentation
- Event-driven architecture with Outbox Pattern
- Full audit trail of entity changes, including automatic versioning history
- DTO validation and exception handling
- Layered architecture following enterprise development practices
- Containerization
- Automated CI/CD pipeline
- Cloud deployment

## Technology Stack

- Java 25
- Spring Boot 4
- Spring Boot Actuator for health, info & metrics endpoints, enabling production monitoring
- Spring Security with JWT for authentication and role-based access control (Admin, Technician, Operator)
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
  - TLS via Let's Encrypt with automated renewal
- Gradle build system with Kotlin DSL
- Swagger / OpenAPI for interactive API documentation & endpoint testing
- Lombok for boilerplate code reduction
