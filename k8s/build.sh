
# Script de construcción para el sistema de votación
mvn clean package -pl apps/auth-service -am -DskipTests=true
mvn clean package -pl apps/users-service -am -DskipTests=true
mvn clean package -pl apps/votes-service -am -DskipTests=true
mvn clean package -pl apps/reports-service -am -DskipTests=true

# Ejecutar los servicios Spring Boot (ejemplo)
java -jar apps/auth-service/target/auth-0.0.1-SNAPSHOT.jar --server.port=8081
java -jar apps/users-service/target/users-0.0.1-SNAPSHOT.jar --server.port=8082
java -jar apps/votes-service/target/votes-0.0.1-SNAPSHOT.jar --server.port=8083
java -jar apps/reports-service/target/reports-0.0.1-SNAPSHOT.jar --server.port=8084

# Build & Push Auth Service
docker build -t localhost:32000/auth-service:latest apps/auth-service
docker push localhost:32000/auth-service:latest

# Build & Push Users Service
docker build -t localhost:32000/users-service:latest apps/users-service
docker push localhost:32000/users-service:latest

# Build & Push Votes Service
docker build -t localhost:32000/votes-service:latest apps/votes-service
docker push localhost:32000/votes-service:latest

# Build & Push Reports Service
docker build -t localhost:32000/reports-service:latest apps/reports-service
docker push localhost:32000/reports-service:latest
