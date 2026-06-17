
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /application

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package


FROM eclipse-temurin:21-jre

WORKDIR /application

COPY --from=build /application/target/*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]

