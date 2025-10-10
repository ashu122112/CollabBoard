# Use an official OpenJDK 17 runtime as the parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# --- THIS IS THE FIX ---
# Add executable permissions to the Maven wrapper script
RUN chmod +x mvnw

# Now that it's executable, run the command to download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of your source code
COPY src ./src

# Build the application JAR file
RUN ./mvnw clean package -DskipTests

# Expose the port your application runs on
EXPOSE 8080

# The command to run your application when the container starts
ENTRYPOINT ["java", "-jar", "target/collabboard-0.0.1-SNAPSHOT.jar"]