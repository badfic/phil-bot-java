FROM ghcr.io/bell-sw/liberica-runtime-container:jdk-21-slim-glibc AS builder

COPY . .
RUN . mvnw package
RUN java -Djarmode=layertools -jar target/philbot-0.0.1-SNAPSHOT.jar extract

FROM ghcr.io/bell-sw/liberica-runtime-container:jre-21-slim-glibc
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-cp BOOT-INF/classes:BOOT-INF/lib/* com.badfic.philbot.BotApplication"]
