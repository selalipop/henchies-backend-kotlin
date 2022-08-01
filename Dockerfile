FROM gradle:latest as gradle

#Cache Gradle Dependencies
COPY server/build.gradle server/gradle.properties server/settings.gradle /opt/src/server/
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

#Build Shadow Jar
COPY . /opt/src
WORKDIR /opt/src/server
RUN ["gradle","shadowJar"]

#Run Shadow Jar
FROM amazoncorretto:latest
WORKDIR /opt/
COPY --from=gradle /opt/src/server/build/libs/server.jar /opt/server.jar
CMD ["java","-jar", "/opt/server.jar"]