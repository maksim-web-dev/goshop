FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :recommendations-service:bootJar --no-daemon -x test --max-workers 1

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/recommendations-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
