FROM openjdk

WORKDIR /microservice1

COPY . .

RUN ./mvnw compile

CMD ["./mvnw", "quarkus:dev"]
