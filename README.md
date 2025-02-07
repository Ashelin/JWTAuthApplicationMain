# JWT Authentication Application

This is a Spring Boot application for authentication and authorization using JWT (JSON Web Token). The project also includes an Angular frontend for user interaction.

## Project Structure

- **Backend**: Spring Boot application
- **Frontend**: Angular application

## Technologies Used

- **Backend**:
    - Java
    - Spring Boot
    - Spring Security
    - JWT
    - Maven
    - PostgreSQL
    - Liquibase

- **Frontend**:
    - TypeScript
    - Angular
    - PrimeNG
    - NPM

## Getting Started

### Prerequisites

- Java 21
- Node.js and NPM
- Docker and Docker Compose

### Running the Application

1. **Clone the repository**:
    ```bash
    git clone https://github.com/Ashelin/JWTAuthApplicationMain
    cd JWTAuthApplicationMain
    ```

2. **Build and run the application using Docker Compose**:
    ```bash
    docker-compose up --build -d
    ```

3. **Access the application**:
    - Backend API: `http://localhost:8080` or `http://localhost:80` via Nginx
    - Frontend: `http://localhost`

## Backend Endpoints

### Authentication

- **POST /api/v1/auth/login**
    - Request Body:
      ```json
      {
        "email": "user@example.com",
        "password": "password"
      }
      ```
    - Response:
      ```json
      {
        "token": "jwt-token",
        "refreshToken": "refresh-token"
      }
      ```

- **POST /api/v1/auth/register**
    - Request Body:
      ```json
      {
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "password": "password"
      }
      ```
    - Response:
      ```json
      {
        "token": "jwt-token",
        "refreshToken": "refresh-token"
      }
      ```
      
- **POST /api/v1/auth/refresh-token**
    - Request Body:
      ```json
      {
        "refreshToken": "refreshToken"
      }
      ```
        - Response:
      ```json
      {
        "token": "new-jwt-token",
        "refreshToken": "new-refresh-token"
      }
      ```

### User Management

- **GET /api/v1/user/search**
    - Request Parameters:
        - `id` (optional)
        - `firstName` (optional)
        - `lastName` (optional)
        - `email` (optional)

    - Response:
    ```json
    [
    {
        "id": 1,
        "firstName": "firstName",
        "lastName": "lastName",
        "email": "email",
        "userRole": "userRole",
        "creationTimestamp": "creationTimestamp",
        "modificationTimestamp": "modificationTimestamp"
    },
    {
        "id": 2,
        "firstName": "firstName",
        "lastName": "lastName",
        "email": "email",
        "userRole": "userRole",
        "creationTimestamp": "creationTimestamp",
        "modificationTimestamp": "modificationTimestamp"
    }
    ]
    ```
  

    - Response: HTTP 200 OK

- **POST /api/v1/user/create**
    - Request Body:
      ```json
      {
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "password": "password"
      }
      ```
      

    - Response: HTTP 201 Created

- **PATCH /api/v1/user/update/{id}**
    - Request Body:
      ```json
      {
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com"
      }
      ```
      

    - Response: HTTP 200 OK

- **DELETE /api/v1/user/delete/{id}**


    - Response: HTTP 200 OK

## Frontend

The frontend is an Angular 19 application that interacts with the backend API for user authentication and management.

### Features

- User login
- User registration
- User management (CRUD operations)

### Running the Frontend

1. **Navigate to the frontend directory**:
    ```bash
    cd JWTAuthApplication-ui
    ```

2. **Install dependencies**:
    ```bash
    npm install
    ```

3. **Run the Angular application**:
    ```bash
    ng serve
    ```

4. **Access the frontend**:
    - `http://localhost:4200` (notice if your using docker-compose, the frontend is served by Nginx at `http://localhost`)

## Docker Configuration

The project includes a `docker-compose.yaml` file to set up the following services:

- **PostgreSQL**: Database for storing user information
- **PgAdmin**: Database management tool
- **Spring Boot Application**: Backend API
- **Angular Builder**: Builds the Angular application
- **Nginx**: Serves the Angular application

## Authors

Daniel Shelest - Ashelin
