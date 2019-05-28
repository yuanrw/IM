package com.yrw.im.rest.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.repository.service.OfflineService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * Date: 2019-05-27
 * Time: 09:52
 *
 * @author yrw
 */
@Component
public class OfflineHandler {

    private OfflineService offlineService;

    public OfflineHandler(OfflineService offlineService) {
        this.offlineService = offlineService;
    }

    public Mono<ServerResponse> listOfflines(ServerRequest request) {

        String userId = request.queryParam("userId").orElseThrow(() -> new ImException("userId can not be empty"));

        Flux<Offline> relationFlux;
        try {
            relationFlux = Flux.fromIterable(offlineService.listOffline(Long.parseLong(userId)));
        } catch (JsonProcessingException e) {
            throw new ImException(e);
        }

        return relationFlux.collect(Collectors.toList()).map(ResultWrapper::success)
            .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }
}
