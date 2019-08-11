package com.github.yuanrw.im.rest.web.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    @Test
    public void pollAllMsg() {
        webClient.get().uri("/offline/poll/1142773797275836418")
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
