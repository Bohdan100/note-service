# Note-service Application
The **Note-service Application** is a demonstration project designed to manage personal notes. Each user has their own notebook(s) and can add, update, view, and delete their notes. Notes can be searched by title or content, and deleted based on keywords in the title. Each note consists of a title and content. Users access the application using their email and password, and can register with their name, email, and password.

## Features
- Authentication and registration via email and password.
- Add, update, view, and delete notes.
- Search notes by title or content.
- Delete notes based on keywords in the title.
- Each user has a personalized notebook.
- Persist note data using MySQL.
- Handle database migrations with Flyway.
- Simplify setup and build processes with Gradle.
- Streamline code using Lombok to reduce boilerplate.
- Ensure code quality and correctness with JUnit 5 and Mockito tests.

## Requirements
The application is built using the following technologies:
- **Spring Boot**: 3.3.5
- **Java Platform (JDK)**: 21
- **MySQL**: 8.0.40
- **Flyway**: 11.0.1
- **Thymeleaf**: 3.4.1
- **Lombok**: 1.18.36
- **JUnit**: 5
- **Mockito**: 5
- **Gradle**: 8.8
  
## Database Setup
Before running the application, follow these steps to set up the database:

1. **Create a MySQL 8.0.40 Database**  
   Set up a MySQL database to store the application’s data.

2. **Configure Database and User**  
   Perform the following steps in your MySQL instance to create a user and database for the application:

    1. Create a new user with a password:
       ```sql
       CREATE USER IF NOT EXISTS 'admin2'@'%' IDENTIFIED BY 'secret1234';
       ```

    2. Create a new database:
       ```sql
       CREATE DATABASE IF NOT EXISTS note;
       ```

    3. Assign ownership of the database to the new user:
       ```sql
       GRANT ALL PRIVILEGES ON note.* TO 'admin2'@'%';
       GRANT SUPER ON *.* TO 'admin2'@'%';
       ```

    4. Check that user has the required ownership:
       ```sql
       SHOW GRANTS FOR 'admin2'@'%';
       ```

    5. Connect to the newly created database:
       ```sql
       USE note;
       ```

3. **Connect to the Database**  
   To connect to the `note` database as the `admin2` user, use the following command in the terminal:
   ```bash
   mysql -u admin2 -p -D note

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Bohdan100/note-service
   cd note-service

2. Build and run the application in terminal using Gradle:
   ```bash
   .\gradlew bootRun     (Windows)
   ./gradlew bootRun     (Linux)

3. Build and run the application in terminal using JAR file:
   ```bash
   .\gradlew bootJar     (Windows)
   ./gradlew bootJar     (Linux)
   
   java -jar note-service.jar
   
4. Work in the application in your browser:
Register: http://localhost:8080/api/v1/auth/register
Login: http://localhost:8080/api/v1/auth/login
Notes:
    Get list: http://localhost:8080/api/v1/note/list
    Add: http://localhost:8080/api/v1/note/add
    Update: http://localhost:8080/api/v1/note/edit/{id}
    Search: http://localhost:8080/api/v1/note/searchTitleAndContent?query={text}

5. Test application
   ```bash
   .\gradlew runAllTests     (Windows)
   ./gradlew runAllTests     (Linux)
   
   
   .\gradlew test            (Windows)
   ./gradlew test            (Linux)