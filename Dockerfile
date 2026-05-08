# ================================
# Stage 1: Build
# ================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml trước để cache dependencies
### dấu . là kiểu khẳng định trước nó là /app còn -b bắt tiến trình chạy theo lô -batch ghi log quan trọng nhất
COPY pom.xml .
RUN mvn dependency:go-offline -B -Dgit-build-hook.skip=true

COPY src ./src
RUN mvn clean package -DskipTests -DskipGitHooks=true

# ================================
# Stage 2: Run
# ================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tạo user non-root (bảo mật)
RUN addgroup -S wmsgroup && adduser -S wmsuser -G wmsgroup
USER wmsuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]