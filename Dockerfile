# Multi-stage Dockerfile for building and running the Spring Boot application with Java 25

# ============================================
# Build Stage: Compile the application
# ============================================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Install Maven
RUN set -eux \
	&& apt-get update \
	&& apt-get install -y maven \
	&& rm -rf /var/lib/apt/lists/*

# Copy pom and source, then build the fat jar
COPY pom.xml ./
COPY src ./src

RUN mvn -B -Dmaven.test.skip=true clean package

# ============================================
# Runtime Stage: Execute the application
# ============================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the produced jar from the build stage
COPY --from=build /workspace/target/*.jar ./app.jar

# Copy data folder for CSV processing
COPY data/ /app/data/

# Expose port
EXPOSE 8080

# Note: Database credentials are passed via environment variables from docker-compose.yml or .env file
# Do NOT hardcode sensitive credentials in this Dockerfile
# Spring will read: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
# from the environment variables set by docker-compose or docker run --env-file

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
