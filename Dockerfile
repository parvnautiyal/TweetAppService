FROM openjdk:17-slim as build
EXPOSE 80
MAINTAINER parvnautiyal
COPY target/TweetApp-0.0.1-SNAPSHOT.jar TweetApp-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","TweetApp-0.0.1-SNAPSHOT.jar"]