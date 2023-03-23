FROM quay.io/badfic/liberica-runtime:jre-17-musl
COPY target/philbot-0.0.1-SNAPSHOT.jar ./philbot.jar
EXPOSE 8080
ENTRYPOINT java -Xms32m -Xmx256m -XX:+UseSerialGC -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml --add-opens java.base/java.io=ALL-UNNAMED -jar philbot.jar