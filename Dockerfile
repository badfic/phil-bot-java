FROM quay.io/badfic/liberica-runtime:jdk-17

COPY . .
RUN . mvnw package -DskipTests

FROM quay.io/badfic/liberica-runtime:jre-17
COPY --from=-0 target/philbot-0.0.1-SNAPSHOT.jar ./philbot.jar
EXPOSE 8080
ENTRYPOINT java -XX:MaxRAM=128m -Xss512k -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml -jar philbot.jar