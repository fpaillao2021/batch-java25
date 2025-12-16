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

RUN mvn -B -DskipTests clean package

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

# Set environment to use Docker network database
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/spring_batch_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=Evertec.2025

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
