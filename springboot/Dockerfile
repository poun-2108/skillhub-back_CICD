FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8000

CMD ["java", "-jar", "target/moustass-auth-service-1.0.0.jar"]