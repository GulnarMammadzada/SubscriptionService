FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/subscription-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]