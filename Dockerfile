# Multi-stage Dockerfile for building and running the Spring Boot application
# Build stage: use Maven with a matching JDK for Java 25
## Build stage: use Temurin JDK 25 and install Maven
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Install Maven (Debian/Ubuntu-based Temurin images) then build
RUN set -eux \
	&& apt-get update \
	&& apt-get install -y maven \
	&& rm -rf /var/lib/apt/lists/*

# Copy pom and source, then build the fat jar
COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests clean package

# Runtime stage: use a lightweight Temurin JRE for Java 25
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the produced jar from the build stage
COPY --from=build /workspace/target/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
