# Single stage build
#FROM maven:3.9.6-eclipse-temurin-17 AS builder
#WORKDIR /app
#COPY . .
#RUN mvn clean package -DskipTests
#
#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY --from=builder /app/target/compare_pg_clickh.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]

#FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
#WORKDIR app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests
#
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR app
#COPY --from=builder app/target/compare_pg_clickh.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre
WORKDIR app
COPY target/compare_pg_clickh.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]