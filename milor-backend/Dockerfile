FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN cd milor-backend && chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/milor-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]