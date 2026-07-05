FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B --no-transfer-progress || true

COPY src ./src
RUN ./mvnw package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN mkdir -p /app/data /app/config /models

COPY --from=builder /build/target/*.jar app.jar

RUN echo '{"provider":"llama","model":"phi-3","temperature":0.7,"max_tokens":2000}' > /app/config/llm_config.json

ENV SERVER_PORT=8000
ENV DATABASE_URL=./data/storyteller.db
ENV JAVA_OPTS="-Xmx1g"

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]