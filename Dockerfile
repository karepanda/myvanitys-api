FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file built by Maven
COPY target/*.jar app.jar

# Environment variables que se necesitan
ENV SPRING_PROFILES_ACTIVE=prod
# Variables adicionales requeridas
ENV GOOGLE_CLIENT_ID=changeme
ENV GOOGLE_CLIENT_SECRET=changeme
ENV OAUTH_REDIRECT_URI=https://myvanitys.com/callback
ENV JWT_SECRET=changeme
# Las variables de PostgreSQL son proporcionadas automáticamente por Railway:
# PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE, DATABASE_URL

# Port to expose (change if your app uses a different port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]