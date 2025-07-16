FROM oeclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/reports-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]