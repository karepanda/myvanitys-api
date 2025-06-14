# Dockerfile para API - Sin credenciales (repositorio público)
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copiar archivos del wrapper
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Dar permisos
RUN chmod +x mvnw

# Copiar pom.xml y dependencias locales para demo
COPY pom.xml .
COPY libs/ libs/

# Instalar la dependencia local en el repositorio Maven del contenedor
RUN ./mvnw install:install-file \
  -Dfile=libs/myvanitys-api-spec-1.8.1-SNAPSHOT.jar \
  -DgroupId=com.myvanitys \
  -DartifactId=myvanitys-api-spec \
  -Dversion=1.8.1-SNAPSHOT \
  -Dpackaging=jar

# Descargar dependencias usando perfil demo
RUN ./mvnw dependency:resolve -Pdemo

# Copiar código fuente
COPY src ./src

# Construir la aplicación usando perfil demo
RUN ./mvnw clean package -DskipTests -Pdemo

# Etapa de runtime
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]