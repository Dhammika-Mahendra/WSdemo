# Use an official JDK 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file to the container
COPY target/WsDemo.jar WsDemo.jar

# Expose the default Spring Boot port
EXPOSE 8088

# Set the command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "WsDemo.jar"]
