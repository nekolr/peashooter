# https://github.com/nekolr/maven-image/tree/master/3.8.5-jdk-17-slim
FROM nekolr/maven:3.8.5-jdk-17-slim AS build

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .
RUN mvn clean package


FROM openjdk:17-slim

COPY --from=build /usr/src/app/target/peashooter.jar .
EXPOSE 8962
CMD java -Djava.security.egd=file:/dev/./urandom -jar peashooter.jar