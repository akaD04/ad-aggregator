# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /build/target/ad-aggregator-*.jar /app/ad-aggregator.jar

# Create directories for data
RUN mkdir -p /data /output && \
    chmod 755 /data /output

# Set reasonable JVM defaults for container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Set entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/ad-aggregator.jar \"$@\"", "--"]