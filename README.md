# Medical Equipment Management System (Radio Registry)

A modern backend application for managing medical imaging equipment (CT scanners, X-ray systems, MRI devices, etc.) within healthcare organizations.

The system allows healthcare providers to register and manage medical devices, associate them with hospitals, and organize them within specific hospital departments such as Radiology, Nuclear Medicine, and Diagnostic Imaging.

> **⚠️ Work in Progress**
>
> This project is currently under active development and serves as a demonstration of modern Java backend development practices.
> New features, improvements, and additional integrations will be added over time.

## Features

- Medical devices management
- Hospital organizations and departments management
- Association between devices, organizations, and departments
- RESTful API architecture
- Authentication and authorization with Spring Security
- API documentation with Swagger/OpenAPI
- Data persistence with PostgreSQL and Hibernate/JPA
- DTO validation and exception handling
- Layered architecture following enterprise development practices

## Technology Stack

- Java 25
- Spring Boot 4
- Spring Boot Actuator for health, info & metrics endpoints, enabling production monitoring
- Spring Security with JWT for authentication and role-based access control (Admin, Technician, Operator)
- Hibernate / JPA
- PostgreSQL
- Audit logging with AOP & Hibernate Envers
- Test suite: 
  - Unit tests (JUnit 5 & Mockito)
  - Slice tests (@WebMvcTest & @DataJpaTest)
  - Integration tests (@SpringBootTest & Testcontainers)
- Environment-based configuration for default & production profiles
- Containerization with Docker & Docker Compose
- Gradle build system with Kotlin DSL
- Swagger / OpenAPI for interactive API documentation & endpoint testing
- Lombok for boilerplate code reduction

## Planned Enhancements

- CI/CD pipeline with GitHub Actions
- Cloud deployment on Oracle Cloud Infrastructure (OCI)

## Purpose

This project was created as a portfolio to demonstrate the development of a production-oriented backend service using modern Java and Spring technologies while applying clean architecture and enterprise development principles.
