# --- Stage 1: Build ---
# Dùng bản JDK để có công cụ biên dịch
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy toàn bộ source code vào
COPY . .

# FIX LỖI WINDOWS: Cài dos2unix để convert line ending của file mvnw
RUN apk add --no-cache dos2unix && dos2unix mvnw

# Cấp quyền thực thi
RUN chmod +x ./mvnw

# Chạy lệnh build (Tạo ra file .jar trong thư mục target)
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Run ---
# Dùng bản JRE cho nhẹ máy khi chạy thật
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Chỉ copy cái file .jar thành phẩm từ Stage 1 sang Stage 2
# (Thay tên file jar cho đúng với project của bạn)
COPY --from=builder /app/target/Inblue-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Chạy file jar
ENTRYPOINT ["java", "-jar", "app.jar"]