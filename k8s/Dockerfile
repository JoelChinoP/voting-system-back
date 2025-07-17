FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 808e

CMD ["java", "-jar", "app.jar"]
