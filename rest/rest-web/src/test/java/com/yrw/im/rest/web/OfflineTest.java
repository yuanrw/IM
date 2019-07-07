package com.yrw.im.rest.web;

import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.rest.web.vo.UserReq;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * Date: 2019-07-05
 * Time: 18:07
 *
 * @author yrw
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
public class OfflineTest {

    @Autowired
    private WebTestClient webClient;

    @BeforeClass
    public static void setup() {
        String propertiesPath = System.getProperty("user.dir") + "/target/test-classes/rest-test.properties";
        System.setProperty("config", propertiesPath);
    }

    @Test
    public void pollAllMsg() {
        UserReq req = new UserReq();
        req.setUsername("yuanrw");
        req.setPwd(DigestUtils.sha256Hex("123abc".getBytes(CharsetUtil.UTF_8)));

        ResultWrapper res = webClient.post().uri("/user/login")
            .body(BodyInserters.fromPublisher(Mono.just(req), UserReq.class))
            .exchange()
            .returnResult(ResultWrapper.class).getResponseBody().blockFirst();

        String token = ((HashMap<String, String>) res.getData()).get("token");

        webClient.get().uri("/offline/poll")
            .header("token", token)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.msg").isEqualTo("SUCCESS")
            .jsonPath("$.data.length()").isEqualTo(2)
            .jsonPath("$.data[0].id").isNotEmpty()
            .jsonPath("$.data[0].toUserId").isNotEmpty()
            .jsonPath("$.data[0].content").isNotEmpty()
            .jsonPath("$.data[1].id").isNotEmpty()
            .jsonPath("$.data[1].toUserId").isNotEmpty()
            .jsonPath("$.data[1].content").isNotEmpty();
    }
}
