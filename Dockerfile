# Use a lightweight Java 17 base image
FROM eclipse-temurin:17-jdk-slim

# Set a working directory inside the container
WORKDIR /app

# Copy the built jar file from your target folder into the container
COPY target/querysystem-0.0.1-SNAPSHOT.jar /app/app.jar

# Tell Render that your app runs on port 8080
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]