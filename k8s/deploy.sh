#!/bin/bash

echo "🚀 Desplegando Sistema de Votación en Kubernetes..."
cd ..

# Verificar que estamos en el directorio correcto
if [ ! -d "apps" ]; then
    echo "❌ Ejecutar desde el directorio raíz del proyecto"
    exit 1
fi

echo "📦 Habilitando registry de MicroK8s..."
microk8s enable registry

echo "🔨 Construyendo y subiendo imágenes Docker..."

# Auth Service
echo "Construyendo auth-service..."
cd apps/auth-service
docker build -t localhost:32000/auth-service:latest .
docker push localhost:32000/auth-service:latest
cd ../..

# Users Service
echo "Construyendo users-service..."
cd apps/users-service
docker build -t localhost:32000/users-service:latest .
docker push localhost:32000/users-service:latest
cd ../..

# Votes Service
echo "Construyendo votes-service..."
cd apps/votes-service
docker build -t localhost:32000/votes-service:latest .
docker push localhost:32000/votes-service:latest
cd ../..

# Reports Service
echo "Construyendo reports-service..."
cd apps/reports-service
docker build -t localhost:32000/reports-service:latest .
docker push localhost:32000/reports-service:latest
cd ../..

echo "☸️ Desplegando en Kubernetes..."
microk8s kubectl apply -f k8s/k8s-deploy.yaml

echo "⏳ Esperando despliegue..."
sleep 30

echo "📊 Estado del despliegue:"
microk8s kubectl get pods -n voting-system
microk8s kubectl get services -n voting-system

echo ""
echo "🌐 URLs de acceso:"
echo "Auth Service: http://localhost:30081"
echo "Users Service: http://localhost:30082" 
echo "Votes Service: http://localhost:30083"
echo "Reports Service: http://localhost:30084"
echo "Load Balancer: http://localhost:30080"