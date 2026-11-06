FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :store-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
