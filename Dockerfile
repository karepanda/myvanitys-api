# Dockerfile con credenciales incluidas (SOLO PARA DEMO DE TESIS)
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Credenciales como argumentos de build
ARG GITHUB_USERNAME=kksven
ARG GITHUB_TOKEN=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

# Copiar archivos del wrapper
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Dar permisos
RUN chmod +x mvnw

# Copiar pom.xml
COPY pom.xml .

# Descargar dependencias con credenciales
RUN ./mvnw dependency:resolve -s .mvn/settings.xml -Dgithub.username="${GITHUB_USERNAME}" -Dgithub.token="${GITHUB_TOKEN}"

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests -s .mvn/settings.xml -Dgithub.username="${GITHUB_USERNAME}" -Dgithub.token="${GITHUB_TOKEN}"

# Etapa de runtime
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]