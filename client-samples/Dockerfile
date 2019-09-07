# Dockerfile for client-samples
# docker build -t yuanrw/client-samples:$VERSION .
# docker run -d --name client-samples client-samples

FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER yuanrw <295415537@qq.com>

ENV SERVICE_NAME client-samples
ENV VERSION 1.0.0

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

COPY src/main/bin/start-docker.sh .
COPY src/main/bin/wait-for-it.sh .

CMD /bin/bash wait-for-it.sh -t 0 connector:9081 --strict -- \
    /bin/bash start-docker.sh