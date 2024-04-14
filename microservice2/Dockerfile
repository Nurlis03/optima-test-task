FROM openjdk

WORKDIR /microservice2

COPY . .

RUN ./mvnw compile

CMD ["./mvnw", "quarkus:dev"]
