FROM openjdk:17-jdk-slim

WORKDIR /app
COPY deploy/telegrambot.jar telegrambot.jar

CMD ["java", "-jar", "telegrambot.jar"]