#!/bin/bash

echo "🚀 Iniciando Users Service (Candidates API)..."
echo "📦 Puerto: 8081"
echo "🗄️  Base de datos: PostgreSQL (localhost:5432)"
echo "📚 Swagger UI: http://localhost:8081/swagger-ui.html"
echo ""

# Verificar si PostgreSQL está ejecutándose
echo "🔍 Verificando PostgreSQL..."
pg_isready -h localhost -p 5432 >/dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL está ejecutándose"
else
    echo "❌ PostgreSQL no está ejecutándose. Asegúrate de que esté iniciado en localhost:5432"
    echo "💡 Comando para iniciar PostgreSQL: brew services start postgresql (macOS) o sudo service postgresql start (Linux)"
    exit 1
fi

echo ""
echo "🏗️  Ejecutando aplicación con perfil de desarrollo..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
