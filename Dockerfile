# Dockerfile con Alpine (más ligero) para Java 23
FROM maven:3-eclipse-temurin-23 AS build

WORKDIR /app

# Copia archivos necesarios
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

# Copia mvnw si existe
COPY mvnw* ./
COPY .mvn/ .mvn/

# Construye aplicación
RUN if [ -f "./mvnw" ]; then \
        chmod +x ./mvnw && ./mvnw clean package -DskipTests; \
    else \
        mvn clean package -DskipTests; \
    fi

# Runtime Alpine (más ligero)
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Instala curl para health checks
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]