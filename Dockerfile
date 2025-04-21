FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file built by Maven
COPY target/*.jar app.jar

# Environment variables that can be overridden
ENV DB_NAME=db
ENV DB_USER=user
ENV DB_PASSWORD=password
ENV SPRING_PROFILES_ACTIVE=prod

# Port to expose (change if your app uses a different port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]