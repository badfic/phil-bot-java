FROM ghcr.io/bell-sw/liberica-runtime-container:jdk-21-slim-musl as builder

COPY . .
RUN . mvnw package
RUN java -Djarmode=layertools -jar target/philbot-0.0.1-SNAPSHOT.jar extract

FROM ghcr.io/bell-sw/liberica-runtime-container:jre-21-slim-musl
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
EXPOSE 8080
ENTRYPOINT java -XX:MaxRAM=512m -Xss512k -XX:MaxMetaspaceSize=128m -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml -cp BOOT-INF/classes:BOOT-INF/lib/* com.badfic.philbot.BotApplication