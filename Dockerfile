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

# Copy pom.xml first to leverage Docker cache
COPY pom.xml ./

# Copy libs directory if exists (contains batch-entity-dto JAR)
# Create empty libs dir to avoid COPY failure
COPY libs* /tmp/libs/

# Install batch-entity-dto JAR if available in libs/
RUN if ls /tmp/libs/batch-entity-dto-*.jar 1> /dev/null 2>&1; then \
        for JAR_FILE in /tmp/libs/batch-entity-dto-*.jar; do \
            mvn install:install-file \
                -Dfile="$JAR_FILE" \
                -DgroupId=com.evertecinc.entitydto \
                -DartifactId=batch-entity-dto \
                -Dversion=1.0.1 \
                -Dpackaging=jar; \
        done; \
    else \
        echo "No batch-entity-dto JAR found in libs/, assuming it's in Maven repo"; \
    fi

# Download dependencies
RUN mvn dependency:resolve -B || true

# Copy source code
COPY src ./src

# Build the application
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

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
