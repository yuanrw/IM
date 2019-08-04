# Dockerfile for rest
# Run with: docker run -p 8080:8080 -d -v /tmp/IM_Logs:/tmp/IM_Logs --name rest rest

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

COPY rest-${VERSION}-bin.zip /rest/rest-${VERSION}-bin.zip

RUN unzip /rest/rest-${VERSION}-bin.zip -d /rest \
    && rm -rf /rest/rest-${VERSION}-bin.zip \
    && cd /rest/rest-${VERSION} \
    && sed -i '$d' start.sh \
    && echo "tail -f /dev/null" >> /start.sh

CMD ["/bin/sh", "start.sh"]