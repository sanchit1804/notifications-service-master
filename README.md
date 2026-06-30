# Notification Service

A mail-only notification microservice built with Spring Boot. Accepts a request to notify a user, persists the message to a database, and sends the email asynchronously via a dedicated thread pool — similar to how a job portal emails you after your application is submitted.

## Features

- REST API to create users and trigger email notifications
- Every notification is persisted with a status (`PENDING` → `SENT`/`FAILED`), so you always have an audit trail of what was sent
- Email dispatch runs on a background `ExecutorService` (fixed thread pool of `Runnable` tasks), so API calls return immediately instead of blocking on SMTP
- Centralized exception handling returns proper HTTP status codes instead of generic 500s

## Tech Stack

- Java 17
- Spring Boot 3.3.4 (Web, Data JPA, Mail)
- H2 (embedded, in-memory database)
- Maven

## Project Structure

```
src/main/java/com/project/
├── config/        # Spring Boot bootstrap + ExecutorService bean
├── controller/     # REST endpoints
├── dto/            # Request payloads
├── exception/      # Custom exceptions + global handler
├── model/          # JPA entities
├── repository/     # Spring Data JPA repositories
├── service/        # Business logic
├── task/           # Runnable submitted to the executor for async email sends
└── utility/service/ # Low-level SMTP sending
```

## Prerequisites

- JDK 17+
- Maven 3.6+
- An SMTP account (e.g. Gmail with an [App Password](https://myaccount.google.com/apppasswords), or a free [Mailtrap](https://mailtrap.io) sandbox inbox for testing)

No database install needed — this uses an embedded H2 in-memory database. Data resets every time the app restarts, which is fine for development/testing. To switch to MySQL/PostgreSQL for real persistence later, swap the `h2` dependency in `pom.xml` and update the `spring.datasource.*` properties accordingly.

## Configuration

All config lives in `src/main/resources/application.properties`.

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:notification_db` | In-memory DB, no install/login needed |
| `spring.mail.host` | `smtp.gmail.com` | SMTP host |
| `spring.mail.username` | — | SMTP account email |
| `spring.mail.password` | — | SMTP account password / app password |
| `notification.executor.pool-size` | `10` | Thread pool size for async email sends |

The mail values are set via environment variables before running:
```bash
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password
```

## Running Locally

```bash
git clone https://github.com/sanchit1804/notification-service.git
cd notification-service

export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password

mvn spring-boot:run
```

The service starts on `http://localhost:8080`. You can browse the in-memory database directly at `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:notification_db`, username `sa`, no password.

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Create a user |
| `GET` | `/users` | List all users |
| `POST` | `/notify/user/{userId}` | Queue and send an email notification |
| `GET` | `/get/user/{userId}/notification` | Get a user's notification history with status |

### Create a user

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Sanchit","email":"you@example.com"}'
```

### Send a notification

```bash
curl -X POST http://localhost:8080/notify/user/1 \
  -H "Content-Type: application/json" \
  -d '{"subject":"Application Received","message":"Your job application has been successfully submitted."}'
```

Returns `202 Accepted` immediately; the email is sent on a background thread.

### Check notification status

```bash
curl http://localhost:8080/get/user/1/notification
```

## How async dispatch works

`NotificationService.notify()` saves the notification as `PENDING`, then submits an `EmailNotificationTask` (a `Runnable`) to a fixed-size `ExecutorService` bean. That task sends the email via SMTP and writes the outcome (`SENT` or `FAILED`, with a failure reason if applicable) back to the same row — all off the original request thread.

