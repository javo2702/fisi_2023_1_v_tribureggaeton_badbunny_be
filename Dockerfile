#FROM openjdk:11-zulu-alpine
FROM adoptopenjdk/openjdk11:latest
COPY target/backend-0.2.jar backend-0.2.jar
ENTRYPOINT ["java","-jar","/backend-0.2.jar"]