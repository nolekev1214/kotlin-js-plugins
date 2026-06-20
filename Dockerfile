FROM docker.io/library/eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q --batch-mode

COPY src ./src
RUN mvn package -DskipTests --batch-mode

FROM docker.io/library/eclipse-temurin:21-jre AS run
WORKDIR /app

COPY --from=build /app/target/untitled-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
