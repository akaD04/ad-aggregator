# Multi-stage build (Java 17)
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy source
COPY src ./src

# Build + tests (optional: keep tests on for CI)
RUN mvn -q -DskipTests=false test package

# Copy runtime dependencies into target/dependency
RUN mvn -q -DskipTests dependency:copy-dependencies -DincludeScope=runtime

# Runtime stage (Java 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar(s) and dependencies
COPY --from=builder /build/target/ /app/target/

# Data dirs (optional)
RUN mkdir -p /data /output && chmod 755 /data /output

# JVM defaults (optional; keep modest by default)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

# Run using classpath (no Main-Class manifest needed)
ENTRYPOINT ["sh","-c", "\
APP_JAR=$(ls /app/target/*.jar | grep -Ev '(sources|javadoc|original)' | head -n 1); \
[ -n \"$APP_JAR\" ] || { echo 'No runnable jar found in /app/target'; ls -lah /app/target; exit 1; }; \
exec java $JAVA_OPTS -cp \"$APP_JAR:/app/target/dependency/*\" vn.flinters.adagg.Main \"$@\" \
", "--"]