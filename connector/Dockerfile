# Dockerfile for connector
# docker run -p 9081:9081 -d -v /tmp/IM_Logs:/tmp/IM_Logs --name connector connector

FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER yuanrw <295415537@qq.com>

ENV VERSION 1.0.0-SNAPSHOT

EXPOSE 9081

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

COPY connector-${VERSION}-bin.zip /connector/connector-${VERSION}-bin.zip

RUN unzip /connector/connector-${VERSION}-bin.zip -d /connector \
    && rm -rf /connector/connector-${VERSION}-bin.zip \
    && cd /connector/connector-${VERSION} \
    && sed -i '$d' start.sh \
    && echo "tail -f /dev/null" >> /start.sh

CMD ["/bin/sh", "start.sh"]