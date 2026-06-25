# ==========================================
# STAGE 1: Build Stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy dependency descriptor first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# STAGE 2: Run Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

VOLUME /tmp

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar

# Render/cloud platforms route traffic to this port by default
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
