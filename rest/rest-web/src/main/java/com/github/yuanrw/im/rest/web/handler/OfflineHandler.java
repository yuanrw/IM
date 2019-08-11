package com.github.yuanrw.im.rest.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.rest.web.service.OfflineService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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

    public Mono<ServerResponse> pollOfflineMsg(ServerRequest request) {

        String id = request.pathVariable("id");

        return Mono.fromSupplier(() -> {
            try {
                return offlineService.pollOfflineMsg(id);
            } catch (JsonProcessingException e) {
                throw new ImException(e);
            }
        }).map(ResultWrapper::success).flatMap(res ->
            ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }
}
