# Dockerfile simple y confiable - usa Maven directo
FROM maven:3-eclipse-temurin-23 AS build

WORKDIR /app

# Copia archivos de configuración Maven (importante para settings.xml)
COPY .mvn/ .mvn/
COPY pom.xml .
COPY libs/ libs/

# Instala dependencia personalizada
RUN mvn install:install-file \
    -Dfile=libs/myvanitys-api-spec-1.8.1-SNAPSHOT.jar \
    -DgroupId=com.myvanitys \
    -DartifactId=myvanitys-api-spec \
    -Dversion=1.8.1-SNAPSHOT \
    -Dpackaging=jar

# Copia código fuente
COPY src/ src/

# Construye aplicación usando Maven directo (siempre funciona)
RUN mvn clean package -DskipTests

# Runtime Alpine optimizado
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Instala herramientas útiles
RUN apk add --no-cache curl tzdata

# Establece zona horaria
ENV TZ=Europe/Madrid

# Crea usuario no-root para seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copia el JAR construido
COPY --from=build /app/target/*.jar app.jar

# Cambia ownership
RUN chown appuser:appgroup app.jar

# Cambiar a usuario no-root
USER appuser

# Variables JVM optimizadas
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]