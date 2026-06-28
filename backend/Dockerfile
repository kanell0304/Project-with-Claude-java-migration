# 빌드 스테이지
FROM maven:3.9-eclipse-temurin-21-slim AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# 실행 스테이지
FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=build /build/target/digital-helper-0.1.0.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
