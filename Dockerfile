FROM openjdk:17-jdk-slim

WORKDIR /app
COPY build/libs/telegrambot.jar telegrambot.jar

CMD ["java", "-jar", "telegrambot.jar"]