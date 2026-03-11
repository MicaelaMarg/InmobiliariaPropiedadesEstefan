# Stage 1: build WAR with Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
# Descargar dependencias (cache si pom no cambia)
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B -q && \
    cp /app/target/inmobiliaria-api.war /app/inmobiliaria-api.war

# Stage 2: Tomcat con el WAR desplegado como ROOT (API en /api)
FROM tomcat:10.1-jdk17-temurin-jammy
WORKDIR /usr/local/tomcat

# Desplegar nuestro WAR como ROOT (context path /) para que la API quede en /api
RUN rm -rf webapps/*

# Copiar WAR desde builder; Tomcat lo expandirá como raíz
COPY --from=builder /app/inmobiliaria-api.war webapps/ROOT.war

# Script para que Tomcat escuche en $PORT (Railway)
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENV CATALINA_HOME=/usr/local/tomcat
EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]
