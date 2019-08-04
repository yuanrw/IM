# Dockerfile for transfer
# docker run -p 9082:9082 -d -v /tmp/IM_Logs:/tmp/IM_Logs --name transfer transfer

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

COPY transfer-${VERSION}-bin.zip /transfer/transfer-${VERSION}-bin.zip

RUN unzip /transfer/transfer-${VERSION}-bin.zip -d /transfer \
    && rm -rf /transfer/transfer-${VERSION}-bin.zip \
    && cd /transfer/transfer-${VERSION} \
    && sed -i '$d' start.sh \
    && echo "tail -f /dev/null" >> /start.sh

CMD ["/bin/sh", "start.sh"]