FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle /app/
COPY gradlew /app/

RUN gradle clean build --dry-run || true

COPY src /app/src/

RUN gradle clean build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

ARG SPRINGBOOT_APP_PROFILE
ENV PROFILE=${SPRINGBOOT_APP_PROFILE:-dev}

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=$PROFILE", "app.jar"]