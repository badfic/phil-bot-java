FROM quay.io/badfic/liberica-openjdk-alpine:17
COPY . .
RUN . mvnw install

FROM quay.io/badfic/liberica-openjdk-alpine:17-jre
COPY --from=0 target/philbot-0.0.1-SNAPSHOT.jar ./philbot.jar
EXPOSE 8080
ENTRYPOINT java -Xms256m -Xmx256m -verbose:gc --add-opens java.base/java.io=ALL-UNNAMED -jar philbot.jar