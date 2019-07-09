package com.yrw.im.rest.web.handler;

import com.google.common.collect.ImmutableMap;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.spi.domain.UserBase;
import com.yrw.im.rest.web.filter.TokenManager;
import com.yrw.im.rest.web.service.RelationService;
import com.yrw.im.rest.web.service.UserService;
import com.yrw.im.rest.web.util.SpiFactory;
import com.yrw.im.rest.web.vo.UserReq;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
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

    private UserSpi<? extends UserBase> userSpi;
    private UserService userService;
    private RelationService relationService;
    private TokenManager tokenManager;

    public UserHandler(SpiFactory spiFactory, UserService userService, RelationService relationService, TokenManager tokenManager) {
        this.userSpi = spiFactory.getUserSpi();
        this.userService = userService;
        this.relationService = relationService;
        this.tokenManager = tokenManager;
    }

    public Mono<ServerResponse> saveUser(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.map(user -> userService.saveUser(user.getUsername(), user.getPwd()))
                    .onErrorMap(e -> new ImException("[rest] username exist"))
                    .switchIfEmpty(Mono.error(new ImException("[rest] save user info failed")))
                    .map(id -> ImmutableMap.of("id", String.valueOf(id)))
                    .map(ResultWrapper::success)
                    .flatMap(id -> ok().contentType(APPLICATION_JSON).body(fromObject(id)))

            , request, UserReq.class);
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.map(login -> userSpi.getUser(login.getUsername(), login.getPwd()))
                    .flatMap(u -> tokenManager.createNewToken(u.getId())
                        .map(t -> {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setId(u.getId());
                            userInfo.setToken(t);
                            return userInfo;
                        }))
                    .flatMap(u -> Flux.fromIterable(relationService.friends(u.getId()))
                        .collectList()
                        .map(list -> {
                            u.setRelations(list);
                            return u;
                        }))
                    .map(ResultWrapper::success)
                    .flatMap(info -> ok().body(fromObject(info)))
                    .switchIfEmpty(Mono.error(new ImException("[rest] authentication failed")))

            , request, UserReq.class);
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        String token = request.headers().header("token").get(0);

        return tokenManager.expire(token).map(ResultWrapper::wrapBol)
            .flatMap(r -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(r)));
    }
}
