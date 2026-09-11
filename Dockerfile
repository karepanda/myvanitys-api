# Simple and reliable Dockerfile - uses direct Maven
FROM maven:3-eclipse-temurin-23 AS build

WORKDIR /app

# Copy Maven configuration files (important for settings.xml)
COPY .mvn/ .mvn/
COPY pom.xml .
COPY libs/ libs/

# Install custom dependency
RUN VERSION=$(mvn help:evaluate -Dexpression=myvanitys-api-spec.version -q -DforceStdout) && \
    mvn install:install-file \
    -Dfile=libs/myvanitys-api-spec-${VERSION}.jar \
    -DgroupId=com.myvanitys \
    -DartifactId=myvanitys-api-spec \
    -Dversion=${VERSION} \
    -Dpackaging=jar

# Copy source code
COPY src/ src/

# Build application using direct Maven (always works)
RUN mvn clean package -DskipTests

# Optimized Alpine Runtime
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Install useful tools
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=Europe/Madrid

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Optimized JVM variables
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]