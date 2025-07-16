

mvn clean package -pl apps/auth-service -am -DskipTests=true
mvn clean package -pl apps/users-service -am -DskipTests=true
mvn clean package -pl apps/votes-service -am -DskipTests=true
mvn clean package -pl apps/reports-service -am -DskipTests=true

java -jar apps/auth-service/target/auth-0.0.1-SNAPSHOT.jar --server.port=8081
java -jar apps/auth-service/target/users-0.0.1-SNAPSHOT.jar --server.port=8082
java -jar apps/auth-service/target/votes-0.0.1-SNAPSHOT.jar --server.port=8083
java -jar apps/auth-service/target/reports-0.0.1-SNAPSHOT.jar --server.port=8084