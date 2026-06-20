# Build Complete Jarfile
FROM docker.io/library/maven:3-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q --batch-mode

COPY src ./src
RUN mvn package -DskipTests --batch-mode

# Setup GraalVM Dependencies
FROM ghcr.io/graalvm/graalvm-community:25 AS graal-runtime
RUN cp -r $JAVA_HOME /graalvm_home

# Setup Distroless Runtime
FROM gcr.io/distroless/base
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENV JAVA_HOME=/opt/graalvm
ENV GRAALVM_HOME=${JAVA_HOME}
ENV PATH=${PATH}:${JAVA_HOME}/bin
COPY --from=graal-runtime /graalvm_home ${JAVA_HOME}

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY plugins ./plugins

ENTRYPOINT ["java"]
CMD ["--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
