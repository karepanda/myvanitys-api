# Dockerfile que maneja correctamente Maven Wrapper
FROM maven:3-eclipse-temurin-23 AS build

WORKDIR /app

# Copia archivos de configuración Maven primero (para mejor cache)
COPY pom.xml .
COPY libs/ libs/

# Copia configuración de Maven Wrapper
COPY .mvn/ .mvn/

# Copia scripts de Maven Wrapper
COPY mvnw* ./

# Asegurar permisos ejecutables para mvnw (importante en Linux)
RUN chmod +x mvnw || true

# Instala dependencia personalizada
RUN mvn install:install-file \
    -Dfile=libs/myvanitys-api-spec-1.8.1-SNAPSHOT.jar \
    -DgroupId=com.myvanitys \
    -DartifactId=myvanitys-api-spec \
    -Dversion=1.8.1-SNAPSHOT \
    -Dpackaging=jar

# Copia código fuente
COPY src/ src/

# Construye aplicación usando Maven Wrapper si existe, sino Maven directo
RUN if [ -f "./mvnw" ] && [ -x "./mvnw" ]; then \
        echo "Usando Maven Wrapper" && \
        ./mvnw clean package -DskipTests; \
    else \
        echo "Usando Maven directo" && \
        mvn clean package -DskipTests; \
    fi

# Runtime Alpine
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Instala curl para health checks
RUN apk add --no-cache curl

# Copia el JAR construido
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]