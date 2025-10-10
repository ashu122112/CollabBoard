# Use an official OpenJDK runtime as a parent image
# We use version 17 to match your pom.xml
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Build the application JAR file inside the container
# This is the same command you ran locally
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Expose the port that your Spring Boot application runs on
EXPOSE 8080

# The command to run your application when the container starts
ENTRYPOINT ["java", "-jar", "target/collabboard-0.0.1-SNAPSHOT.jar"]