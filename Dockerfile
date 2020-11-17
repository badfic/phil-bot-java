FROM registry.access.redhat.com/ubi8/openjdk-8
USER root
WORKDIR /home
COPY . /home
RUN ["./mvnw", "install"]

FROM gcr.io/distroless/java:8
COPY --from=0 /home/target/philbot-0.0.1-SNAPSHOT.jar .
CMD ["philbot-0.0.1-SNAPSHOT.jar"]