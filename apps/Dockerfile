# Multi-stage build for smaller final image
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy the entire project (using the correct relative path)
COPY food_bot/ .

# Make mvnw executable and build the application
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B && \
    ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Use exec form for better signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]