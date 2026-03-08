# EcclesiaFlow Communication Module

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![gRPC](https://img.shields.io/badge/gRPC-1.60.0-blue.svg)](https://grpc.io/)
[![MySQL](https://img.shields.io/badge/MySQL-9.0.0-blue.svg)](https://dev.mysql.com/downloads/mysql/)

> Transactional email service for the EcclesiaFlow platform.

---

## Overview

Microservice for email sending and tracking: Thymeleaf templates, multi-provider support (Gmail SMTP + SendGrid fallback), RabbitMQ queue processing, and gRPC inter-service API. Clean Architecture with 4 layers.

**Consumers**: Members Module (gRPC client on port 9092).

> **Note**: Password-reset emails are handled directly by Keycloak via its own SMTP and custom email themes. This module does **not** send password-reset emails.

```
Members Module ──gRPC──▶ Communication Module ──SMTP──▶ Gmail / SendGrid
                            │
                            ├─ Queue (RabbitMQ)
                            ├─ Render (Thymeleaf)
                            └─ Track (DB)
```

---

## Email Templates

| Template | Proto Enum | Trigger |
|----------|-----------|---------|
| `welcome.html` | `EMAIL_TEMPLATE_WELCOME` | Member onboarding complete |
| `confirmation.html` | `EMAIL_TEMPLATE_EMAIL_CONFIRMATION` | New member registration |
| `password-changed.html` | `EMAIL_TEMPLATE_PASSWORD_CHANGED` | Member password changed |
| `profile-updated.html` | `EMAIL_TEMPLATE_PROFILE_UPDATED` | Member profile updated |

All templates extend `base.html` (v2 Bold & Branded layout with dark header, accent color strip, and branded footer).

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 9.0+
- RabbitMQ (optional — async processing)

### Setup

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE email_module_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Configure environment
cp .env.example .env
# Edit .env with your Gmail credentials, DB credentials, etc.

# 3. Compile (generates gRPC/OpenAPI sources)
mvn clean compile

# 4. Run
mvn spring-boot:run

# 5. Verify
curl http://localhost:8082/actuator/health
open http://localhost:8082/swagger-ui.html
```

---

## Architecture

```
com.ecclesiaflow.communication/
├── application/      Config, async, RabbitMQ, AOP logging
├── business/         Services, domain, ports, exceptions
├── io/               JPA persistence, email providers, gRPC server
└── web/              REST delegate, DTOs, exception handler
```

**Ports**: `8082` (REST) / `9092` (gRPC)

---

## Configuration

All secrets come from environment variables (`.env` file via spring-dotenv). See `.env.example` for the full list.

Key variables:

| Variable | Purpose |
|----------|---------|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Gmail SMTP (primary provider) |
| `SENDGRID_API_KEY`, `SENDGRID_ENABLED` | SendGrid (fallback provider) |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_ENABLED` | RabbitMQ async queue |
| `GRPC_SERVER_PORT` | gRPC server port (default 9092) |
| `EMAIL_LOGO_URL` | Logo URL injected into email templates |

---

## gRPC API

Defined in `src/main/proto/email_service.proto`:

| RPC | Description |
|-----|-------------|
| `SendEmail` | Send a single email |
| `GetEmailStatus` | Get delivery status by email ID |
| `SendBulkEmails` | Batch send |

Email statuses: `QUEUED → SENT → DELIVERED / FAILED / BOUNCED`

---

## Testing

```bash
mvn test                      # Unit tests
mvn clean verify              # Tests + coverage
open target/site/jacoco/index.html  # View report
```

---

## Commit Convention

```
Subject in English, imperative, first letter capitalized (≤ 50 chars)

Optional body (≤ 72 chars/line) — explain the why, not the what.
```

Examples:
- `Remove obsolete password-reset template`
- `Add SendGrid provider fallback`
- `Fix RabbitMQ retry queue configuration`

---

## License

MIT License — See [LICENSE](LICENSE)
