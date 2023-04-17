FROM ibm-semeru-runtimes:open-17-jre
COPY target/philbot-0.0.1-SNAPSHOT.jar ./philbot.jar
EXPOSE 8080
ENTRYPOINT java -Xms32m -Xmx64m -XX:MaxRAM=128m -Xss512k -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -verbose:gc -Dspring.backgroundpreinitializer.ignore=true -Dspring.config.location=classpath:application.yaml -jar philbot.jar