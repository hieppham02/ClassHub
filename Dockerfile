# Giai đoạn 1: Tải máy ảo JDK về để build code thành file .jar
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . /app
# Cấp quyền chạy cho file Maven Wrapper (tránh lỗi Permission denied)
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Giai đoạn 2: Lấy file .jar vừa build ra để chạy (dùng bản JRE cho nhẹ server)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]