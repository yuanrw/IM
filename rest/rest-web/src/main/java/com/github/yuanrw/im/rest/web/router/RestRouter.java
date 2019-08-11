package com.github.yuanrw.im.rest.web.router;

import com.github.yuanrw.im.rest.web.handler.OfflineHandler;
import com.github.yuanrw.im.rest.web.handler.RelationHandler;
import com.github.yuanrw.im.rest.web.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Date: 2019-02-09
 * Time: 12:56
 *
 * @author yrw
 */
@Configuration
public class RestRouter {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler userHandler) {
        return RouterFunctions
            .route(POST("/user/login").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                userHandler::login)
            .andRoute(GET("/user/logout").and(accept(APPLICATION_JSON)),
                userHandler::logout);
    }

    @Bean
    public RouterFunction<ServerResponse> relationRoutes(RelationHandler relationHandler) {
        return RouterFunctions
            .route(GET("/relation/{id}").and(accept(APPLICATION_JSON)),
                relationHandler::listFriends)
            .andRoute(GET("/relation").and(accept(APPLICATION_JSON)),
                relationHandler::getRelation)
            .andRoute(POST("/relation").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                relationHandler::saveRelation)
            .andRoute(DELETE("/relation/{id}").and(accept(APPLICATION_JSON)),
                relationHandler::deleteRelation);
    }

    @Bean
    public RouterFunction<ServerResponse> offlineRoutes(OfflineHandler offlineHandler) {
        //only for connector
        return RouterFunctions
            .route(GET("/offline/poll/{id}").and(accept(APPLICATION_JSON)),
                offlineHandler::pollOfflineMsg);
    }
}
