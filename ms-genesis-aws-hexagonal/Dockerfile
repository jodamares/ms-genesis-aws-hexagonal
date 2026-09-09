# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mkdir -p shared src/template apps/backend
COPY shared/pom.xml shared/pom.xml
COPY src/template/pom.xml src/template/pom.xml
COPY apps/backend/pom.xml apps/backend/pom.xml

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml,required=false \
    mvn -B -pl apps/backend -am dependency:go-offline || true

COPY . .

RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml,required=false \
    mvn -B -pl apps/backend -am clean package

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="local-dev" \
      app="ms-organization-template"

ENV TZ=America/Lima \
    LANG=C.UTF-8 \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=local \
    SPRING_CLOUD_CONFIG_ENABLED=false \
    SPRING_CLOUD_BOOTSTRAP_ENABLED=false

RUN addgroup -S appcontainer && adduser -S app -G appcontainer && \
    apk add --no-cache tzdata wget && \
    mkdir -p /opt/app && chown -R app:appcontainer /opt/app

WORKDIR /opt/app

COPY --from=build --chown=app:appcontainer /workspace/apps/backend/target/template.jar app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health >/dev/null 2>&1 || \
      wget -qO- http://localhost:8080/custom-path/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/app/app.jar"]
