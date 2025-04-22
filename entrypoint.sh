#!/bin/bash
set -e

# Extraer host y puerto de DATABASE_URL
if [[ -n "$DATABASE_URL" ]]; then
  DB_HOST=$(echo $DATABASE_URL | sed -E 's/^.*:\/\/(.*):([0-9]+)\/.*$/\1/')
  DB_PORT=$(echo $DATABASE_URL | sed -E 's/^.*:\/\/(.*):([0-9]+)\/.*$/\2/')
else
  DB_HOST=$PGHOST
  DB_PORT=$PGPORT
fi

# Esperar a que la base de datos esté disponible
echo "Esperando que la base de datos esté disponible en $DB_HOST:$DB_PORT..."
until nc -z $DB_HOST $DB_PORT; do
  echo "Base de datos no disponible, esperando..."
  sleep 2
done
echo "Base de datos disponible, continuando..."

# Ejecutar migraciones de Flyway
echo "Ejecutando migraciones de base de datos con Flyway..."
flyway -url="${FLYWAY_URL}" \
       -user="${FLYWAY_USER}" \
       -password="${FLYWAY_PASSWORD}" \
       -locations="${FLYWAY_LOCATIONS}" \
       migrate

# Verificar resultado de Flyway
if [ $? -eq 0 ]; then
  echo "Migraciones ejecutadas correctamente"
else
  echo "Error al ejecutar migraciones con Flyway"
  exit 1
fi

# Iniciar la aplicación Spring Boot
echo "Iniciando la aplicación..."
exec java ${JAVA_OPTS} -jar /app/app.jar