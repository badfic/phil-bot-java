FROM ghcr.io/bell-sw/liberica-runtime-container:jdk-21-slim-glibc AS builder

COPY . .
RUN . mvnw package
RUN java -Djarmode=tools -jar target/philbot-0.0.1-SNAPSHOT.jar extract --layers --launcher --destination target/layers

FROM ghcr.io/bell-sw/liberica-runtime-container:jre-21-slim-glibc
COPY --from=builder target/layers/dependencies/ ./
COPY --from=builder target/layers/spring-boot-loader/ ./
COPY --from=builder target/layers/snapshot-dependencies/ ./
COPY --from=builder target/layers/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "com.badfic.philbot.BotApplication"]
