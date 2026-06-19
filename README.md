📩 Notification Service

A Spring Boot-based backend service for managing users, subscriptions, and notifications.
It provides REST APIs to send notifications, manage users, and handle subscription plans using an in-memory HSQL database.

🚀 Features
Send notifications to users
Retrieve all users
Fetch notifications for a specific user
Subscribe users to plans
In-memory database (HSQL) for quick setup
RESTful API design
🛠 Tech Stack
Java 8+
Spring Boot
Spring Web
Spring Data JPA
HSQLDB (In-memory database)
Maven
📁 Project Structure
com.project
│
├── controller        # REST APIs
├── service           # Business logic
├── repository        # DB layer (Spring Data JPA)
├── model             # Entities
├── dto               # Request/Response objects
├── config            # App configuration
└── exception         # Custom exceptions
▶️ How to Run
1. Clone the project
git clone <repo-url>
cd notification-service
2. Build the project
mvn clean install
3. Run the application
mvn spring-boot:run
4. App will start at:
http://localhost:8080
📌 API Endpoints
👤 Users
Get all users
GET /users
🔔 Notifications
Send notification to user
POST /users/{userId}/notifications

Request Body:

{
  "message": "Hello User",
  "notificationType": "EMAIL"
}
Get user notifications
GET /users/{userId}/notifications
💳 Subscriptions
Subscribe user to plan
POST /subscriptions

Request Body:

{
  "userId": "123",
  "plan": "PREMIUM"
}
🧪 Testing with Postman
Example: Send Notification
Method: POST
URL:
http://localhost:8080/users/1/notifications
Body:
{
  "message": "Test notification",
  "notificationType": "EMAIL"
}
