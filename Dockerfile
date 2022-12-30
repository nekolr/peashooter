#FROM maven:3.8.5-openjdk-17-slim AS build
# https://github.com/nekolr/maven-image/tree/master/3.8.5-jdk-17-slim
FROM nekolr/maven:3.8.5-jdk-17-slim AS build

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .
RUN apt-get update \
    && apt install curl -y  \
    && curl -sL https://deb.nodesource.com/setup_16.x | bash -  \
    && apt-get install nodejs -y
RUN mvn clean package


FROM openjdk:17-slim

ENV PEASHOOTER_USERNAME=admin \
    PEASHOOTER_PASSWORD=admin \
    TZ=Asia/Shanghai \
    HOME_DIR=/data

RUN useradd -r -U -m -d ${HOME_DIR} peashooter

COPY --chmod=755 --from=build /usr/src/app/target/peashooter.jar ${HOME_DIR}
RUN mkdir -p ${HOME_DIR}/peashooter
EXPOSE 8962
WORKDIR $HOME_DIR
VOLUME $HOME_DIR/peashooter

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "peashooter.jar"]