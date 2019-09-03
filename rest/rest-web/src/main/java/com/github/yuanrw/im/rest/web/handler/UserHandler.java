package com.github.yuanrw.im.rest.web.handler;

import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.rest.spi.UserSpi;
import com.github.yuanrw.im.rest.spi.domain.UserBase;
import com.github.yuanrw.im.rest.web.filter.TokenManager;
import com.github.yuanrw.im.rest.web.service.RelationService;
import com.github.yuanrw.im.rest.web.spi.SpiFactory;
import com.github.yuanrw.im.rest.web.vo.UserReq;
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
    private RelationService relationService;
    private TokenManager tokenManager;

    public UserHandler(SpiFactory spiFactory, RelationService relationService, TokenManager tokenManager) {
        this.userSpi = spiFactory.getUserSpi();
        this.relationService = relationService;
        this.tokenManager = tokenManager;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.flatMap(login -> {
                    UserBase user = userSpi.getUser(login.getUsername(), login.getPwd());
                    return user != null ? Mono.just(user) : Mono.empty();
                })
                    .flatMap(u -> tokenManager.createNewToken(u.getId())
                        .map(t -> {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setId(u.getId());
                            userInfo.setUsername(u.getUsername());
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
