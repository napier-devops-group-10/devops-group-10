
# Group 10 – Containerisation Documentation

## 1. Project Overview

This project is part of the SET09803 DevOps coursework at Edinburgh Napier University.

The Group 10 project is a Population Reporting System developed using Java, Maven, MySQL and Docker.

The containerisation component provides a consistent environment for building and running the Java application alongside a MySQL database.

## 2. Technologies Used

- Java 17
- Apache Maven
- Docker
- Docker Compose
- MySQL 8.0
- GitHub Actions

## 3. Containerisation Architecture

The application uses two Docker services:

### Java Application (app)

The Java application is built using a multi-stage Dockerfile.

The first stage uses Maven and Java 17 to compile and package the application.

The second stage uses a Java 17 runtime image to execute the packaged JAR.

### MySQL Database (db)

The MySQL 8.0 container provides the database service.

The database is configured through Docker Compose and uses a persistent Docker volume to store its data.

The application communicates with MySQL through Docker's internal network using the hostname `db` and port `3306`.

## 4. Prerequisites

Before running the application, ensure the following are available:

- Docker Desktop or Docker Engine with Docker Compose
- Git
- Access to the Group 10 GitHub repository

Java and Maven do not need to be installed locally when building the application through Docker.

## 5. Environment Configuration

Create a file named `.env` in the project root.

Add the following configuration:

```env
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_USER=population_user
MYSQL_PASSWORD=your_database_password
```

Replace the example passwords with your own values.

The `.env` file must not be committed to GitHub because it contains database credentials.

The MySQL database name is configured as `world` in docker-compose.yml.

## 6. Building the Docker Image

Open a terminal in the project root and execute:

```bash
docker build -t devops-group10-app .
```

This command builds the Java application using the multi-stage Dockerfile and creates the application image.

## 7. Starting the Application and Database

Start the Docker Compose services:

```bash
docker compose up -d --build
```

This command builds the Java application image and starts the application and MySQL containers.

Check the container status:

```bash
docker compose ps -a
```

The MySQL container should report a healthy status after initialization.

The current Java application prints `Boo yah!` and exits successfully. Therefore, an application container status of `Exited (0)` is expected.

## 8. Verifying the MySQL Database

Connect to MySQL:

```bash
docker compose exec db mysql -u population_user -p world
```

Enter the database password when prompted.

Execute:

```sql
SELECT DATABASE();
```

Expected result:

```text
world
```

Exit MySQL:

text.
exit;
```

## 9. Testing the Java-to-MySQL Connection

Run the Java database connection test inside Docker:

```bash
docker compose run --rm --no-deps --entrypoint java app -cp app.jar com.napier.sem.DatabaseConnectionTest
```

Expected output:

```text
Successfully connected to MySQL!
Connected database: world
```

This confirms that the Java application can communicate with the MySQL database through Docker Compose.

The MySQL database container must be running and healthy before executing this test.

## 10. GitHub Actions CI/CD

The GitHub Actions workflow is located at:

.github/workflows/docker-build.yml

The workflow is configured to:

1. Check out the repository.
2. Configure Java 17.
3. Build the Java application using Maven.
4. Build the Docker image.
5. Run the Java application inside Docker.

The workflow is triggered by pushes to the feature/containerisation branch and pull requests targeting develop.

The workflow configuration has been committed locally. Execution on GitHub remains pending until repository write access is available.

## 11. Stopping the Containers

To stop and remove the Docker Compose containers:

```bash
docker compose down
```

The database volume is preserved by default.

Do not use `docker compose down -v` unless you intentionally want to remove the database volume and its stored data.

## 12. Containerisation Deliverables

The following deliverables have been prepared:

- Multi-stage Dockerfile
- Docker Compose configuration
- Containerised MySQL connection test
- GitHub Actions Docker build workflow
- Containerisation documentation

The Docker image, Compose configuration and Java-to-MySQL connection have been tested locally.