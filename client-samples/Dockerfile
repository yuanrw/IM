# Dockerfile for client-samples
# docker build -f Dockerfile -t client-samples target
# docker run --name client-samples client-samples

FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER yuanrw <295415537@qq.com>

ENV SERVICE_NAME client-samples
ENV VERSION 1.0.0-SNAPSHOT

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

COPY ${SERVICE_NAME}-${VERSION}-bin.zip /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip

RUN unzip /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip -d /${SERVICE_NAME} \
    && rm -rf /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION}-bin.zip \
    && cd /${SERVICE_NAME}/${SERVICE_NAME}-${VERSION} \
    && sed -i '$d' start.sh \
    && echo "tail -f /dev/null" >> /start.sh

CMD ["/bin/sh", "start.sh"]