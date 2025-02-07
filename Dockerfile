FROM openjdk:21

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw package

EXPOSE 8080

LABEL authors="Caio"

ENTRYPOINT ["java", "-jar", "-Xmx16G", "/app/target/pdf_conversions_api.jar"]

ENV GOOGLE_RUNTIME_VERSION=23
ENV PORT=8080
