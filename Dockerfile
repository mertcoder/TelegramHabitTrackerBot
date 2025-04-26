FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . .

RUN ./gradlew shadowJar

CMD ["java", "-jar", "build/libs/telegrambot.jar"]