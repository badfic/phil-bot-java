FROM maven:3-jdk-8-alpine
WORKDIR /app
COPY . /app
RUN ["mvn", "install"]

FROM amazoncorretto:8-alpine-jre
COPY --from=0 /app/target/philbot-0.0.1-SNAPSHOT.jar .

CMD java -jar philbot-0.0.1-SNAPSHOT.jar