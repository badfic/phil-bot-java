FROM ghcr.io/badfic/liberica-runtime-mirror:jdk-17-slim-glibc as builder

COPY . .
RUN . mvnw package -DskipTests
RUN java -Djarmode=layertools -jar target/philbot-0.0.1-SNAPSHOT.jar extract

FROM ghcr.io/badfic/liberica-runtime-mirror:jre-17-slim-glibc
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
EXPOSE 8080
ENTRYPOINT java -XX:MaxRAM=256m -Xss512k -XX:MaxMetaspaceSize=128m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml -cp BOOT-INF/classes:BOOT-INF/lib/* com.badfic.philbot.BotApplication