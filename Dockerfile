
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test

# Runtime stage
FROM openjdk:17-jdk
WORKDIR /app

ARG PORT
ARG MONGO_URI
ARG MONGO_DATA_BASE

ENV PORT=${PORT}
ENV MONGO_URI=${MONGO_URI}
ENV MONGO_DATA_BASE=${MONGO_DATA_BASE}

COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE ${PORT}
CMD ["java", "-jar", "/app/app.jar"]