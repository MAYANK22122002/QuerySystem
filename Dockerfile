# --- STAGE 1: The Builder ---
# Use an official Maven image (which includes Java 17) to build the project
FROM maven:3.9.6-eclipse-temurin-17-jammy AS builder

# Set the working directory for the build
WORKDIR /build

# Copy the entire project into the build container
COPY . .

# Run Maven to build the .jar file
RUN mvn clean install


# --- STAGE 2: The Runner ---
# Use the lightweight Amazon image to run the app
FROM amazoncorretto:17-al2-jdk

# Set the working directory for the running app
WORKDIR /app

# Copy the built jar file FROM the 'builder' stage
# Make sure 'querysystem-0.0.1-SNAPSHOT.jar' matches your pom.xml
COPY --from=builder /build/target/querysystem-0.0.1-SNAPSHOT.jar /app/app.jar

# Tell Render that your app runs on port 8080
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]