# Etapa de compilación
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos todo el contenido del repositorio
COPY . .

# Nos movemos a la carpeta del backend y compilamos
WORKDIR /app/milor-backend
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/milor-backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]