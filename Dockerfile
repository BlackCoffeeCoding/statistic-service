# Используем легковесный Linux с Java 17
FROM eclipse-temurin:17-jre-alpine

# Рабочая папка внутри контейнера
WORKDIR /app

# Создаем пользователя, чтобы не работать под root (безопасность)
RUN addgroup -S javauser && adduser -S javauser -G javauser

# Копируем JAR файл (Maven сложил его в target/)
# ARG позволяет гибко менять имя файла, но по умолчанию ищем любой .jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Права доступа
RUN chown javauser:javauser /app/app.jar
USER javauser

# Документируем порт
EXPOSE 8084

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]