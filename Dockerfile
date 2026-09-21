
# Stage 1: Build the Java application using Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the Java source code and build the executable JAR
COPY src ./src
RUN mvn clean package


# Stage 2: Run the application using Java 17
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the executable JAR from the Maven build stage
COPY --from=build /app/target/devops-cw.jar app.jar

# Start the Java application
ENTRYPOINT ["java", "-jar", "app.jar"]