# PetMusicStorage

## Project Overview
**PetMusicStorage** is a project that offers music streaming services based on user subscription levels. The project consists of two main parts:

1. **Music Streaming Service**:  
   Users can register and choose from three types of subscriptions: `FREE`, `OPTIMAL`, and `MAXIMUM`. Based on the selected subscription, users are assigned roles that grant access to music pages specific to their subscription level. Upon registration, users are granted the `MAXIMUM` subscription, which they can manage from their personal dashboard. Subscriptions have expiration dates and prices, and users can change their subscription, listen to music, and update personal information from the dashboard.

2. **Banking System Simulation**:  
   This simulates a banking server that processes payment requests from the main application. It validates the account details and payment amounts for subscriptions and returns a response based on the payment processing.

---

## Technologies & Tools
The project leverages the following key technologies, frameworks, and tools:

1. **Spring Boot**: The main framework for building web applications and microservices. The following Spring Boot starters are used:
    - `spring-boot-starter-data-jpa`: For working with databases using JPA and Hibernate.
    - `spring-boot-starter-web`: For creating RESTful web services.
    - `spring-boot-starter-security`: For securing the application.
    - `spring-boot-starter-thymeleaf`: For rendering HTML templates.
    - `spring-boot-starter-mail`: For sending emails.

2. **JPA (Hibernate)**: Used to interact with the database using Object-Relational Mapping (ORM), allowing seamless data manipulation as Java objects.

3. **Databases**:
    - **PostgreSQL**: Used in the main application for storing user data, subscriptions, etc.
    - **MySQL**: Used in the banking system simulation for transaction management.

4. **Liquibase and Flyway**: Tools for managing database schema versions:
    - **Liquibase**: Used for managing the main application's PostgreSQL database.
    - **Flyway**: Used for managing the MySQL database in the banking system.

5. **JWT (JSON Web Tokens)**: Used for user authentication through token-based mechanisms.

6. **Spring Security**: Provides authentication and authorization services, including role-based access control for users.

7. **Telegram Bots API**: Integration with Telegram via the `telegrambots` library for implementing chatbot functionalities.

8. **H2 Database**: An in-memory database used for testing purposes.

9. **Thymeleaf**: A template engine for rendering dynamic HTML pages.

10. **OpenAPI (Swagger)**: Used to auto-generate API documentation using `springdoc-openapi-ui`.

11. **JUnit & Mockito**: Tools for writing and running unit tests, including tests with mocks (using Mockito).

12. **Maven**: Project management and build automation tool used for managing dependencies, building the project, and running tests.

---

## Project Requirements
Before running the project, ensure you have the following installed:

- **Java 17**: The project is configured to run with Java version 17.
- **Maven**: For managing dependencies and building the project.
- **PostgreSQL**: The main application uses PostgreSQL as the primary database.
- **MySQL**: The banking system simulation uses MySQL.
- **Git** (optional): For cloning the project repository.

---

## Running the Project
The project consists of two parts, each requiring its own server:

1. **Main Application (Port 8080)**:
    - Provides music streaming services.
    - Manages user registration, subscriptions, and personal data.

2. **Banking System Simulation (Port 8081)**:
    - Handles payment processing for subscription upgrades (`OPTIMAL` and `MAXIMUM`).
    - This service runs in the background and communicates with the main application to process payment requests.
    - It does not have a user interface.

### Steps to Run:
1. Clone the project repository using Git.
2. Set up both PostgreSQL and MySQL databases according to the project configuration.
3. Build and run both the main application and the banking system using Maven.
4. Use **ngrok** to make the application accessible externally (`ngrok http 8080`).

---

## Swagger API Documentation
The main application comes with Swagger documentation, allowing you to explore the available endpoints. You can access the Swagger UI by navigating to: http://localhost:8080/swagger-ui.html


---


## Contribution
If you are interested in practicing or learning front-end development, feel free to reach out for collaboration on creating a custom front-end for this project. Contact me on [LinkedIn](https://www.linkedin.com/in/pavelpavlik/).

---

## Testing
The project uses **JUnit** and **Mockito** for unit and integration testing. In-memory databases (like H2) are used to run the tests without needing an external database.

---

## Author
- **Pavel Pavlik** - [LinkedIn](https://www.linkedin.com/in/pavelpavlik/)


---
## YouTube
- **Short video review in Ukrainian** - [YouTube](https://www.youtube.com/watch?v=zO3D6wFv3dc&t=1s)
