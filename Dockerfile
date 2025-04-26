FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . .

RUN ./gradlew build

CMD ["java", "-jar", "build/libs/telegrambot-1.0-SNAPSHOT.jar"]