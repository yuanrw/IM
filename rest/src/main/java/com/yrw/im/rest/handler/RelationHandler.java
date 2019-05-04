package com.yrw.im.rest.handler;

import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.repository.service.RelationService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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

        Flux<Relation> relationFlux = Flux.fromIterable(relationService.friends(Long.parseLong(id)));

        return relationFlux.collect(Collectors.toList()).map(ResultWrapper::success)
            .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }
}
