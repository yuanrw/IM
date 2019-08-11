package com.github.yuanrw.im.rest.web.test;

import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.rest.web.filter.TokenManager;
import com.github.yuanrw.im.rest.web.vo.UserReq;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * Date: 2019-06-16
 * Time: 22:52
 *
 * @author yrw
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
public class UserTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private TokenManager tokenManager;

    @Before
    public void mock() {
        String token = UUID.randomUUID().toString();

        Mockito.when(tokenManager.createNewToken(anyString())).thenReturn(Mono.just(token));
        Mockito.when(tokenManager.expire(anyString())).thenReturn(Mono.just(true));
        Mockito.when(tokenManager.validateToken(token)).thenReturn(Mono.just("123"));
    }

    @Test
    public void testLogin() {
        UserReq req = new UserReq();
        req.setUsername("yuanrw");
        req.setPwd(DigestUtils.sha256Hex("123abc".getBytes(CharsetUtil.UTF_8)));

        webClient.post().uri("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(req))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.msg").isEqualTo("SUCCESS")
            .jsonPath("$.data.id").exists()
            .jsonPath("$.data.token").exists();
    }

    @Test
    public void testLogout() {
        UserReq req = new UserReq();
        req.setUsername("yuanrw");
        req.setPwd(DigestUtils.sha256Hex("123abc".getBytes(CharsetUtil.UTF_8)));

        ResultWrapper res = webClient.post().uri("/user/login")
            .body(BodyInserters.fromPublisher(Mono.just(req), UserReq.class))
            .exchange()
            .returnResult(ResultWrapper.class).getResponseBody().blockFirst();

        String token = ((HashMap<String, String>) res.getData()).get("token");

        webClient.get().uri("/user/logout")
            .header("token", token)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.msg").isEqualTo("SUCCESS");
    }
}
