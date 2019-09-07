# Dockerfile for connector
# docker build -t yuanrw/connector:$VERSION .
# docker run -p 9081:9081 -d -v /tmp/IM_logs:/tmp/IM_logs --name connector connector

FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER yuanrw <295415537@qq.com>

ENV SERVICE_NAME connector
ENV VERSION 1.0.0

EXPOSE 9081

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

COPY target/${SERVICE_NAME}-${VERSION}-bin.zip /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip

RUN unzip /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip -d /${SERVICE_NAME} \
    && rm -rf /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip \
    && cd /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION} \
    && echo "tail -f /dev/null" >> start.sh

WORKDIR /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}

COPY src/main/resources/connector-docker.properties .
COPY src/main/bin/start-docker.sh .
COPY src/main/bin/wait-for-it.sh .

CMD /bin/bash wait-for-it.sh -t 0 transfer:9082 --strict -- \
    /bin/bash start-docker.sh