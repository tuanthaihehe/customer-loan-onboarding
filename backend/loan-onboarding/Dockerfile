# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper và pom.xml trước để cache dependencies
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (layer được cache nếu pom.xml không đổi)
RUN ./mvnw dependency:go-offline -q

# Copy source và build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo user non-root để chạy app (bảo mật)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/loan-onboarding-*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
