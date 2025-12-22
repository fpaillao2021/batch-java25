#!/bin/bash

echo "═══════════════════════════════════════════════════════════"
echo "🧪 PRUEBA DE APLICACIÓN BATCH-JAVA25"
echo "═══════════════════════════════════════════════════════════"
echo ""

# Verificar que las bases de datos estén corriendo
echo "1️⃣ Verificando que las bases de datos estén corriendo..."
echo ""

# Verificar MySQL
echo "   📊 Verificando MySQL (puerto 3308)..."
if nc -z localhost 3308 2>/dev/null; then
    echo "   ✅ MySQL está corriendo"
else
    echo "   ❌ MySQL NO está corriendo. Por favor ejecuta: docker-compose up -d mysql"
    exit 1
fi

# Verificar PostgreSQL
echo "   📊 Verificando PostgreSQL (puerto 5433)..."
if nc -z localhost 5433 2>/dev/null; then
    echo "   ✅ PostgreSQL está corriendo"
else
    echo "   ❌ PostgreSQL NO está corriendo. Por favor ejecuta: docker-compose up -d postgres"
    exit 1
fi

echo ""
echo "2️⃣ Compilando la aplicación..."
mvn -B -Dmaven.test.skip=true clean package

if [ $? -ne 0 ]; then
    echo "   ❌ Error al compilar la aplicación"
    exit 1
fi

echo ""
echo "   ✅ Compilación exitosa"
echo ""

echo "3️⃣ Iniciando la aplicación..."
echo "   ⏳ Espera unos segundos mientras la aplicación inicia..."
echo ""

# Iniciar la aplicación en background
mvn spring-boot:run -Dspring-boot.run.profiles=local > app.log 2>&1 &
APP_PID=$!

# Esperar a que la aplicación inicie
echo "   ⏳ Esperando a que la aplicación inicie (máximo 60 segundos)..."
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "   ✅ Aplicación iniciada correctamente (PID: $APP_PID)"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "   ❌ La aplicación no inició en 60 segundos"
        echo "   📋 Últimas líneas del log:"
        tail -20 app.log
        kill $APP_PID 2>/dev/null
        exit 1
    fi
    sleep 1
done

echo ""
echo "4️⃣ Probando el endpoint de batch..."
echo ""

# Probar con DB_A (MySQL)
echo "   🧪 Probando inserción en DB_A (MySQL)..."
RESPONSE_A=$(curl -s -X POST "http://localhost:8080/api/batch/ejecutar/registros.csv" \
    -H "X-Database: DB_A" \
    -H "Content-Type: application/json")

echo "   📋 Respuesta DB_A: $RESPONSE_A"
echo ""

# Esperar un poco antes de la siguiente prueba
sleep 3

# Probar con DB_B (PostgreSQL)
echo "   🧪 Probando inserción en DB_B (PostgreSQL)..."
RESPONSE_B=$(curl -s -X POST "http://localhost:8080/api/batch/ejecutar/registros.csv" \
    -H "X-Database: DB_B" \
    -H "Content-Type: application/json")

echo "   📋 Respuesta DB_B: $RESPONSE_B"
echo ""

# Esperar un poco antes de verificar datos
sleep 3

echo "5️⃣ Verificando datos en las bases de datos..."
echo ""

# Verificar MySQL
echo "   📊 Verificando MySQL..."
MYSQL_COUNT=$(mysql -h localhost -P 3308 -u root -pEvertec.2025 spring_batch_db -e "SELECT COUNT(*) as total FROM registrocsv;" 2>/dev/null | tail -1)
echo "   ✅ MySQL tiene $MYSQL_COUNT registros"

# Verificar PostgreSQL
echo "   📊 Verificando PostgreSQL..."
POSTGRES_COUNT=$(PGPASSWORD=Evertec.2025 psql -h localhost -p 5433 -U postgres -d spring_batch_db -t -c "SELECT COUNT(*) FROM registrocsv;" 2>/dev/null | xargs)
echo "   ✅ PostgreSQL tiene $POSTGRES_COUNT registros"

echo ""
echo "6️⃣ Deteniendo la aplicación..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null
echo "   ✅ Aplicación detenida"

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ PRUEBA COMPLETADA"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "📋 Logs de la aplicación guardados en: app.log"
echo ""

