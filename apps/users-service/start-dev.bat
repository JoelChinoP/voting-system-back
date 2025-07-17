@echo off
echo 🚀 Iniciando Users Service (Candidates API)...
echo 📦 Puerto: 8081
echo 🗄️  Base de datos: PostgreSQL (localhost:5432)
echo 📚 Swagger UI: http://localhost:8081/swagger-ui.html
echo.

echo 🔍 Verificando PostgreSQL...
pg_isready -h localhost -p 5432 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ PostgreSQL está ejecutándose
) else (
    echo ❌ PostgreSQL no está ejecutándose. Asegúrate de que esté iniciado en localhost:5432
    echo 💡 Comando para iniciar PostgreSQL: net start postgresql-x64-XX (Windows con servicio)
    pause
    exit /b 1
)

echo.
echo 🏗️  Ejecutando aplicación con perfil de desarrollo...
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

pause
