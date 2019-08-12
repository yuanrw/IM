package com.github.yuanrw.im.rest.web.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.rest.web.service.RelationService;
import com.github.yuanrw.im.rest.web.vo.RelationReq;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * Date: 2019-02-11
 * Time: 14:50
 *
 * @author yrw
 */
@Component
public class RelationHandler {

    private RelationService relationService;

    public RelationHandler(RelationService relationService) {
        this.relationService = relationService;
    }

    public Mono<ServerResponse> listFriends(ServerRequest request) {

        String id = request.pathVariable("id");

        return Flux.fromIterable(relationService.friends(id))
            .collectList()
            .map(ResultWrapper::success)
            .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }

    public Mono<ServerResponse> getRelation(ServerRequest request) {
        String u1 = request.queryParam("userId1").orElseThrow(() -> new ImException("parameter userId1 can not be null"));
        String u2 = request.queryParam("userId2").orElseThrow(() -> new ImException("parameter userId2 can not be null"));

        Long userId1 = Long.parseLong(u1);
        Long userId2 = Long.parseLong(u2);

        Mono<Relation> relationMono = Mono.fromCallable(() -> relationService.getOne(new LambdaQueryWrapper<Relation>()
            .eq(Relation::getUserId1, Math.min(userId1, userId2))
            .eq(Relation::getUserId2, Math.max(userId1, userId2))));

        return relationMono.map(ResultWrapper::success)
            .flatMap(r -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(r)))
            .switchIfEmpty(Mono.error(new ImException("no relation")));
    }

    public Mono<ServerResponse> saveRelation(ServerRequest request) {
        return ValidHandler.requireValidBody(req ->

                req.map(r -> relationService.saveRelation(r.getUserId1(), r.getUserId2()))
                    .map(id -> ImmutableMap.of("id", String.valueOf(id)))
                    .map(ResultWrapper::success)
                    .flatMap(r -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(r)))

            , request, RelationReq.class);
    }

    public Mono<ServerResponse> deleteRelation(ServerRequest request) {

        String id = request.pathVariable("id");

        return request.bodyToMono(RelationReq.class)
            .flatMap(r -> Mono.fromCallable(() -> relationService.removeById(id)))
            .map(ResultWrapper::wrapBol)
            .defaultIfEmpty(ResultWrapper.success())
            .flatMap(r -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(r)));
    }
}