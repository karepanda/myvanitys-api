# Usa una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim as builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR empaquetado desde el directorio target de tu proyecto local
COPY boot/target/boot-0.0.1-SNAPSHOT.jar /app/myvanitys-api.jar

# Exponer el puerto en el que tu aplicación va a escuchar
EXPOSE 8080

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "myvanitys-api.jar"]
