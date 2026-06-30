# Notification Service (mail-only)

Spring Boot 3.3.4 / Java 17 / MySQL via JPA / explicit `ExecutorService` for async email dispatch.

## What changed from your version

**Bugs fixed**
- `NotificationDTO`: `getnotificationType`/`setnotificationType` (lowercase `n`) broke Jackson's bean-property matching for JSON bodies — Jackson needs `getNotificationType`/`setNotificationType` to bind a `notificationType` JSON field. Since the project is mail-only now, this field was dropped entirely and replaced with `subject`.
- `INofiticationRepository` → renamed `INotificationRepository` (typo).
- IDs were `Short` (max value 32,767) on both `Notification` and the repository generic — switched to `Long`.
- `NotificationController` used `org.springframework.util.StringUtils.isEmpty`, which is deprecated/removed in current Spring — replaced with `ObjectUtils.isEmpty` checks.
- `EmailService` caught `MailException` and only logged it (silently swallowing send failures) while a separate generic `catch (Exception)` rethrew — inconsistent. Now every failure is rethrown as `EmailException` so the caller can record it.
- Hardcoded `SENDER_MAIL` and `MAIL_SUBJECT` — sender now comes from `spring.mail.username`, subject comes from the request (or defaults to "Notification").
- `NotificationService`, `NotificationController`, and `UserController` referenced classes that didn't exist anywhere in what you sent: `User`, `IUserService`/`UserService`, `UserNotFoundException`, plus a `Plan`/`PhoneType`/`IPushService`/`SmsService`/`PushException`/`SmsException` cluster for SMS and push notifications. Since you said the project is mail-only, I removed the SMS/push branch entirely instead of inventing those classes, and added the `User` pieces that genuinely are needed.
- Every method declared `throws BaseException` but nothing ever caught it — added `GlobalExceptionHandler` so a failure returns a real HTTP status + message instead of a generic 500.

**Database**
- Removed `db/DatabaseConfig.java` (in-memory HSQL with seed SQL scripts). The datasource is now MySQL, configured entirely through `application.properties`, auto-wired by Spring Boot — no manual `DataSource` bean needed.
- `spring.jpa.hibernate.ddl-auto=update` so tables are created/updated automatically from the entities on startup. No manual schema file needed.
- Added `status` (`PENDING` / `SENT` / `FAILED`), `subject`, `createdAt`, `sentAt`, and `failureReason` to `Notification` so the DB actually reflects what was attempted and whether it succeeded — that's the "save mail messages that are to be sent" part.

**Multithreading**
- Replaced `@Async` on `EmailService` with an explicit `ExecutorService` bean (`ExecutorConfig`, fixed thread pool, size configurable via `notification.executor.pool-size`).
- `NotificationService.notify()` saves the `Notification` row as `PENDING` immediately, then submits a `new EmailNotificationTask(...)` (implements `Runnable`) to that executor. The task sends the email and writes back `SENT` or `FAILED` on its own thread, so the HTTP request returns immediately (`202 Accepted`) without waiting on SMTP.

**One assumption I made:** I picked MySQL (your stack notes mention MySQL/PostgreSQL) and Spring Boot 3.3.4 with `jakarta.persistence` (matching your URL Shortener project) instead of the `javax.persistence` in what you pasted, since Spring Boot 2.x is end-of-life. If you want PostgreSQL instead, swap the `mysql-connector-j` dependency for `org.postgresql:postgresql` and adjust the `spring.datasource.url`/dialect — say the word and I'll do it.

## Setup

1. **MySQL** — have a MySQL server running locally (or remote). The schema/database itself (`notification_db`) is auto-created by the JDBC URL's `createDatabaseIfNotExist=true`; you just need MySQL running and reachable.
2. **Gmail SMTP (or any SMTP provider)** — if using Gmail, enable 2FA on the account and generate an [App Password](https://myaccount.google.com/apppasswords); that 16-character value goes in `spring.mail.password`, not your real Gmail password.
3. Set these via environment variables (recommended) or edit `application.properties` directly:

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=notification_db
DB_USERNAME=root
DB_PASSWORD=yourpassword

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=you@gmail.com
MAIL_PASSWORD=your-app-password
```

4. Run it:
```
mvn spring-boot:run
```

## Testing end to end

Create a user:
```
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Sanchit","email":"your-real-inbox@example.com"}'
```
Note the returned `id`.

Trigger a notification email (e.g. the "application sent" use case):
```
curl -X POST http://localhost:8080/notify/user/1 \
  -H "Content-Type: application/json" \
  -d '{"subject":"Application Received","message":"Your job application has been successfully submitted."}'
```
This returns `202 Accepted` immediately; the email is sent on a background thread.

Check what got recorded (status will be `SENT` or `FAILED` once the background thread finishes):
```
curl http://localhost:8080/get/user/1/notification
```

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/users` | create a user |
| GET | `/users` | list users |
| POST | `/notify/user/{userId}` | queue + send an email notification |
| GET | `/get/user/{userId}/notification` | list a user's notification history with status |
