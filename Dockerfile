FROM eclipse-temurin:21-jdk-jammy AS compilacion
WORKDIR /proyecto
COPY gradlew build.gradle settings.gradle ./
COPY gradle/ gradle/
COPY src/main/ src/main/
RUN sh gradlew --no-daemon bootWar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=compilacion /proyecto/build/libs/discografia-1.war /app/discografia.war
USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/discografia.war"]
