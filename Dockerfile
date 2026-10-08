# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Pre-download Maven dependencies and cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar ./country-info-st-v1.jar

RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "country-info-st-v1.jar"]

