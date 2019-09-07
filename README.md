# IM
[![Build Status](https://travis-ci.org/yuanrw/IM.svg?branch=master)](https://travis-ci.org/yuanrw/IM)
[![codecov](https://codecov.io/gh/yuanrw/IM/branch/master/graph/badge.svg)](https://codecov.io/gh/yuanrw/IM)
[![codebeat badge](https://codebeat.co/badges/be93165e-76f7-473b-ae45-7c8b5e7b7a87)](https://codebeat.co/projects/github-com-yuanrw-im-master)
[![release](https://img.shields.io/github/v/release/yuanrw/IM?include_prereleases)](https://github.com/yuanrw/IM/releases)
[![last commit](https://img.shields.io/github/last-commit/yuanrw/IM)]()

IM is a lightweight instant messaging server. It also provides a client jar,allows you to develop your own client.For example,with spring boot. It's able to login with your own login system or with ldap.

## Features
* One to one text/file message
* Sent/Delivered/Read message
* Ldap Authentication
* Authenticate with individual login system
* Horizontal expansion
* Provide client jar

## Quick Start

### Prepare
We use docker to quick start IM.

```
# detect if the docker environment is avaliable.
docker -v
```
```
# clone the repository
git clone git@github.com:yuanrw/IM.git
```

### Start

```
cd IM/docker
docker-compose up
```

There is a simple sample in the container,it starts serveral clients and send messages to their friends randomly,printing logs which are similar with followed:

```
......
2019-08-11 17:29:13.451 client-samples - [Olive] get a msg: 357980857883037697 has been read
2019-08-11 17:29:13.452 client-samples - [yuanrw] get a msg: 357980857887232002 has been read
2019-08-11 17:29:13.452 client-samples - [xianyy] get a msg: 357980857887232001 has been read
2019-08-11 17:29:13.452 client-samples - [Adela] get a msg: 357980857874649089 has been read
2019-08-11 17:29:13.452 client-samples - [Bella] get a msg: 357980857874649090 has been read
2019-08-11 17:29:13.452 client-samples - [Tom] get a msg: 357980857887232000 has been read



sentMsg: 51, readMsg: 51, hasSentAck: 51, hasDeliveredAck: 51, hasReadAck: 51, hasException: 0



2019-08-11 17:29:15.114 client-samples - [Bella]get a msg: 357980866275840002 has been sent
2019-08-11 17:29:15.114 client-samples - [Adela]get a msg: 357980866275840000 has been sent
2019-08-11 17:29:15.114 client-samples - [Cynthia]get a msg: 357980866275840003 has been sent
......
```

## Distributed Deploy
```
mvn clean package -DskipTests
```
get $SERVICE_NAME-$VERSION-bin.zip under dir /target

### Environment Requirement
* java 8+
* mysql 5.7+
* rabbitmq
* redis

### Start
Start services with **the following order**:
rest-web --> transfer -->connector

Here are the steps for start rest-web,transfer and connector are similar with it.

#### rest-web
1. Unzip

```
unzip rest-web-$VERSION-bin.zip
cd rest-web-$VERSION
```

2. Update the config file

```
server.port=8082

# your log path
log.path=

# your jdbc config
spring.datasource.url=
......

# your redis config
spring.redis.host=
......

# your rabbitmq config
spring.rabbitmq.host=
......
```

3. run the sql in the file `rest.sql`

4. start server

```
java -jar rest-web-$VERSION.jar --spring.config.location=application.properties
```

#### transfer
```
java -jar -Dconfig=transfer.properties transfer-$VERSION.jar
```

#### connector
```
java -jar -Dconfig=connector.properties connector-$VERSION.jar
```

## Nginx Config
All services are available to expand horizontally,connections need to be kept alive between each client and connector server.
A sample nginx config:

```
stream {
	upstream backend {
        # connector services port
        server 127.0.0.1:9081         max_fails=3 fail_timeout=30s;
        server 127.0.0.1:19081			max_fails=3 fail_timeout=30s;
	}

    server {
        # to keep a persistent connection
        listen 9999 so_keepalive=on;
        proxy_timeout 1d;
        proxy_pass backend;
    }
}
```

## Login
There is a simple usable login system in IM. 
IM also support the following two ways to authenticate.

### ldap
We use open ldap as an example.
update application.properties
```
spi.user.impl.class=com.yrw.im.rest.web.spi.impl.LdapUserSpiImpl

# the following config should be replace with your own config
spring.ldap.base=dc=example,dc=org
# admin
spring.ldap.username=cn=admin,dc=example,dc=org
spring.ldap.password=admin
spring.ldap.urls=ldap://127.0.0.1:389
# user filter，use the filter to search user when login in
spring.ldap.searchFilter=
# search base eg. ou=dev
ldap.searchBase=
# user objectClass
ldap.mapping.objectClass=inetOrgPerson
ldap.mapping.loginId=uid
ldap.mapping.userDisplayName=gecos
ldap.mapping.email=mail
```
```
java -jar rest-web-$VERSION.jar --spring.config.location=application.properties
```
### individual login system
1. Implement the spi in `com.yrw.im.rest.spi.UserSpi`

```
public interface UserSpi<T extends UserBase> {

    /**
     * get user by username and password, return user(id can not be null)
     * if username and password are right, else return null.
     * <p>
     * be sure that your password has been properly encrypted
     *
     * @param username
     * @param pwd
     * @return
     */
    T getUser(String username, String pwd);

    /**
     * get user by id, if id not exist then return null.
     *
     * @param id
     * @return
     */
    T getById(String id);
}
```

2. Update application.properties

```
# your implement class full name
spi.user.impl.class=
```

3. Build

```
mvn clean package -DskipTests
```

## Use client jar
A client demo:

[MyClient.java](https://github.com/yuanrw/IM/blob/master/client-samples/src/main/java/com/github/yuanrw/im/client/sample/MyClient.java)