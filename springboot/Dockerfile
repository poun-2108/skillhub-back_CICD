# syntax=docker/dockerfile:1

# ── Stage 1 : Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Cache des dépendances Maven — ne se rebuild que si pom.xml change.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw \
 && ./mvnw -B -ntp dependency:go-offline

# Packaging de l'application.
COPY src src
RUN ./mvnw -B -ntp clean package -DskipTests \
 && cp target/*.jar target/app.jar

# ── Stage 2 : Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Exécution en utilisateur non privilégié.
RUN groupadd --system spring \
 && useradd  --system --gid spring spring

COPY --from=build --chown=spring:spring /app/target/app.jar /app/app.jar

USER spring:spring

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
