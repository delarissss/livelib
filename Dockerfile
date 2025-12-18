# Dockerfile
# Используем официальный OpenJDK 17 образ как базовый
FROM eclipse-temurin:21-jdk-jammy

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл из папки target в контейнер
COPY target/livelib-0.0.1-SNAPSHOT.jar app.jar

# Указываем команду для запуска приложения
# Spring Boot будет использовать переменные окружения из docker-compose.yml
ENTRYPOINT ["java", "-jar", "app.jar"]