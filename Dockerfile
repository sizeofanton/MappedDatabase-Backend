FROM openjdk:8-jre-alpine

ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/MappedDatabaseBackend-0.0.1-all.jar /app/MappedDatabaseBackend-0.0.1-all.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UseG1GC", "-jar", "MappedDatabaseBackend-0.0.1-all.jar"]

