# =========================
# 1️⃣ Build stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar pom.xml primero (mejora cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del proyecto
COPY src ./src

# Build del jar
RUN mvn clean package -DskipTests

# =========================
# 2️⃣ Runtime stage
# =========================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar el jar desde el builder
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]