FROM ghcr.io/bell-sw/liberica-runtime-container:jdk-21-slim-musl as builder

COPY . .
RUN . mvnw package
RUN java -Djarmode=layertools -jar target/philbot-0.0.1-SNAPSHOT.jar extract

# Specifically setting the JRE container to the arm64 sha to force it
FROM ghcr.io/bell-sw/liberica-runtime-container:jre-21-slim-musl@sha256:d6948f5b8c84525c51cdc1702e6c036ae103d1899133380841cf8eac1f363d54
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
EXPOSE 8080
ENTRYPOINT java -XX:MaxRAM=512m -Xss512k -XX:MaxMetaspaceSize=128m -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml -cp BOOT-INF/classes:BOOT-INF/lib/* com.badfic.philbot.BotApplication