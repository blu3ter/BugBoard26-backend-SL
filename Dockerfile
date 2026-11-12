# ==========================================
# STAGE 1: Build dell'applicazione con Maven
# ==========================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# 1. Copiamo solo il pom.xml per scaricare e mettere in cache le dipendenze
#    (così se modifichi solo il codice Java, Docker non riscarica tutto da capo)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiamo il codice sorgente e compiliamo il pacchetto .jar (saltando i test)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# STAGE 2: Immagine leggera di Runtime (JRE)
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Creiamo un utente non-root per maggiore sicurezza
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamo solo il .jar compilato dallo stage precedente
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

