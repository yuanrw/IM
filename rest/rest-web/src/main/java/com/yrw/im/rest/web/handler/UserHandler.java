package com.yrw.im.rest.web.handler;

import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.repository.service.UserService;
import com.yrw.im.rest.web.session.RedisSession;
import com.yrw.im.rest.web.vo.UserReq;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * Date: 2019-02-09
 * Time: 15:03
 *
 * @author yrw
 */
@Component
public class UserHandler {

    private UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.map(user -> userService.saveUser(user.getUsername(), user.getPwdSha()))
                    .map(String::valueOf)
                    .map(ResultWrapper::success)
                    .flatMap(id -> ok().contentType(APPLICATION_JSON).body(fromObject(id)))

            , request, UserReq.class);
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.flatMap(login -> Mono.fromSupplier(() -> userService.verifyAndGet(login.getUsername(), login.getPwdSha())))
                    .map(u -> new UserInfo(u.getId()))
                    .flatMap(u -> Mono.fromSupplier(() -> {
                        u.setToken(RedisSession.createSession(u.getUserId()).getId());
                        return u;
                    }))
                    .map(ResultWrapper::success)
                    .flatMap(info -> ok().body(fromObject(info)))
                    .switchIfEmpty(Mono.error(new ImException("[rest] authentication failed")))

            , request, UserReq.class);
    }

    public Mono<ServerResponse> logout(ServerRequest request) {

        String sessionId = request.headers().header("token").get(0);

        return Mono.fromCallable(() -> RedisSession.getById(sessionId))
            .flatMap(session -> Mono.fromCallable(session::expire))
            .map(ResultWrapper::success)
            .flatMap(ignore -> ok().build());
    }
}
