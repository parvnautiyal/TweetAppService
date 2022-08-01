FROM openjdk:18-slim as build
EXPOSE 8080
MAINTAINER parvnautiyal
COPY target/TweetApp-0.0.1-SNAPSHOT.jar TweetApp-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","TweetApp-0.0.1-SNAPSHOT.jar"]