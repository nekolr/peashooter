FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /usr/src/app
COPY . .

# 安装 Node.js
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && curl -sL https://deb.nodesource.com/setup_24.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*


RUN mvn clean package

# 运行阶段
FROM eclipse-temurin:25-jre

ENV PEASHOOTER_USERNAME=admin \
    PEASHOOTER_PASSWORD=admin \
    TZ=Asia/Shanghai \
    HOME_DIR=/data

RUN useradd -r -U -m -d ${HOME_DIR} peashooter

WORKDIR ${HOME_DIR}
COPY --chmod=755 --from=backend-build /usr/src/app/target/peashooter.jar .
RUN mkdir -p peashooter && chown -R peashooter:peashooter ${HOME_DIR}

EXPOSE 8962
VOLUME ${HOME_DIR}/peashooter

USER peashooter

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "peashooter.jar"]
