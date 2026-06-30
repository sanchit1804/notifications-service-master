# Notification Service

A mail-only notification microservice built with Spring Boot. Accepts a request to notify a user, persists the message to MySQL, and sends the email asynchronously via a dedicated thread pool — similar to how a job portal emails you after your application is submitted.

## Features

- REST API to create users and trigger email notifications
- Every notification is persisted with a status (`PENDING` → `SENT`/`FAILED`), so you always have an audit trail of what was sent
- Email dispatch runs on a background `ExecutorService` (fixed thread pool of `Runnable` tasks), so API calls return immediately instead of blocking on SMTP
- Centralized exception handling returns proper HTTP status codes instead of generic 500s

## Tech Stack

- Java 17
- Spring Boot 3.3.4 (Web, Data JPA, Mail)
- MySQL
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
- A running MySQL instance
- An SMTP account (e.g. Gmail with an [App Password](https://myaccount.google.com/apppasswords))

## Configuration

All config lives in `src/main/resources/application.properties` and can be overridden with environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `notification_db` | Database name (auto-created if missing) |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | SMTP account email |
| `MAIL_PASSWORD` | — | SMTP account password / app password |
| `NOTIFICATION_POOL_SIZE` | `10` | Thread pool size for async email sends |

## Running Locally

```bash
git clone https://github.com/sanchit1804/notification-service.git
cd notification-service

export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password

mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

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

