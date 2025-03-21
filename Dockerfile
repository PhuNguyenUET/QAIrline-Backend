FROM openjdk:17-jdk-alpine

RUN apk add --no-cache curl netcat-openbsd

RUN addgroup -S qairline && adduser -S phunh -G qairline

USER phunh:qairline

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

COPY mail_template /mail_template

ENTRYPOINT ["java", "-jar", "/app.jar"]