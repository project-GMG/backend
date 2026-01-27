FROM --platform=$BUILDPLATFORM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
RUN gradle dependencies --no-daemon || true

COPY src ./src/
RUN gradle clean build -x test --no-daemon

FROM --platform=$TARGETPLATFORM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update -qqy && \
    apt-get install -qqy --no-install-recommends ca-certificates curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /certs && \
    curl -sSL -o /certs/global-bundle.pem \
        https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem && \
    keytool -importcert -noprompt -trustcacerts \
        -alias aws-rds-global-bundle \
        -file /certs/global-bundle.pem \
        -keystore $JAVA_HOME/lib/security/cacerts \
        -storepass changeit
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]