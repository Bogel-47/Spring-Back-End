# For Java 17, try this
FROM openjdk:11

# Refer to Maven build -> finalName
ARG JAR_FILE=target/demo-0.0.1-SNAPSHOT.jar

EXPOSE 8090
# cd /opt/app
WORKDIR /opt/app

# cp target/springboot-3-0.0.1-SNAPSHOT.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]
