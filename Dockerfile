FROM quay.io/badfic/zulu-openjdk-alpine:15
COPY . .
RUN . mvnw install

FROM quay.io/badfic/zulu-openjdk-alpine:15-jre
COPY --from=0 target/philbot-0.0.1-SNAPSHOT.jar ./philbot.jar
EXPOSE 8080
CMD ["-Xms256m", "-Xmx256m", "-verbose:gc", "-jar", "philbot.jar"]
ENTRYPOINT ["java"]