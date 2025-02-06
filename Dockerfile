FROM openjdk:23

WORKDIR /app

COPY . .

RUN ./mvnw package

EXPOSE 8010

LABEL authors="Caio"
ENTRYPOINT ["java", "-jar", "-Xmx16G","/app/target/.jar"]

ENV GOOGLE_RUNTIME_VERSION 23
ENV PORT 8010