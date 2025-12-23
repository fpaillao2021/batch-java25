#!/bin/bash
# Script para compilar y ejecutar los servicios Docker

set -e

echo "=============================================="
echo "1. Compilando batch-entity-dto..."
echo "=============================================="
cd ../batch-entity-dto
mvn clean install -DskipTests
echo "✅ batch-entity-dto compilado"

echo "=============================================="
echo "2. Copiando JAR a ambos proyectos..."
echo "=============================================="
# Copiar a batch-java25
cd ../batch-java25
mkdir -p libs
cp ../batch-entity-dto/target/batch-entity-dto-*.jar libs/
echo "✅ JAR copiado a batch-java25/libs/"

# Copiar a batch-dl-data-mysql
cd ../batch-dl-data-mysql
mkdir -p libs
cp ../batch-entity-dto/target/batch-entity-dto-*.jar libs/
echo "✅ JAR copiado a batch-dl-data-mysql/libs/"

echo "=============================================="
echo "3. Construyendo y levantando servicios Docker..."
echo "=============================================="
cd ../batch-java25
docker-compose down -v 2>/dev/null || true
docker-compose up --build -d

echo "=============================================="
echo "4. Limpiando archivos temporales..."
echo "=============================================="
rm -rf libs/batch-entity-dto-*.jar
rm -rf ../batch-dl-data-mysql/libs/batch-entity-dto-*.jar

echo "=============================================="
echo "✅ Servicios Docker iniciados correctamente"
echo "=============================================="
echo ""
echo "Verificar logs: docker-compose logs -f"
echo "API batch-java25: http://localhost:8080"
echo "API batch-dl-data-mysql: http://localhost:8585"
echo "MySQL: localhost:3308"
