FROM eclipse-temurin:21 AS builder
WORKDIR /app
COPY ./food_bot .
RUN ./mvnw package

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 