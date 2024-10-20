# Expense Tracker Application

## Overview
This is a simple web-based expense tracking application built using Spring Boot and Thymeleaf. The application allows users to register, log in, and manage their expenses by adding and deleting expenses. Each user has a personalized dashboard where they can view their total expenses.

## Features
- User Registration and Authentication (using Spring Security).
- Add and delete expenses for the logged-in user.
- View total expenses per user.
- Secure password storage (BCrypt).
- Simple responsive UI using Thymeleaf templates.

## Technologies Used
- **Java**: Core programming language.
- **Spring Boot**: Main framework for building the application.
- **Spring Security**: For authentication and password encryption.
- **Thymeleaf**: Template engine for dynamic HTML views.
- **JPA/Hibernate**: For interacting with the database.
- **MySQL**: Database for storing users and expenses.
- **BCrypt**: For encrypting passwords.

## Installation and Setup

### Prerequisites
- JDK 17 or higher
- MySQL
- Maven (for build management)

### Setup MySQL Database
1. Install MySQL and create a database:
    ```bash
    CREATE DATABASE webapp;
    ```
2. Update the `src/main/resources/application.properties` file with your MySQL credentials:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/webapp
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password
    spring.jpa.hibernate.ddl-auto=update
    ```

### Run the Application
1. Clone the repository:
    ```bash
    git clone https://github.com/your_username/expense-tracker.git
    cd expense-tracker
    ```
2. Build the application using Maven:
    ```bash
    mvn clean install
    ```
3. Run the Spring Boot application:
    ```bash
    mvn spring-boot:run
    ```
4. Access the application at `http://localhost:8080`.

### Usage
- Register a new user at `/register`.
- Log in using the newly created account at `/login`.
- Add or delete expenses from the `/home` page.
