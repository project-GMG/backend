FROM gradle:8.5-jdk21-jammy AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src/
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update -y && \
    apt-get install -y ca-certificates curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /certs && \
    echo "Downloading RDS certificate..." && \
    curl -sSL -o /certs/global-bundle.pem \
        https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem && \
    echo "Importing certificate to Java truststore..." && \
    keytool -importcert -noprompt -trustcacerts \
        -alias aws-rds-global \
        -file /certs/global-bundle.pem \
        -keystore ${JAVA_HOME}/lib/security/cacerts \
        -storepass changeit && \
    echo "Verifying certificate import..." && \
    keytool -list -keystore ${JAVA_HOME}/lib/security/cacerts \
        -storepass changeit -alias aws-rds-global && \
    echo "Certificate import completed successfully"

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]