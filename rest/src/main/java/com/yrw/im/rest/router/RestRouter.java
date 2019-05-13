package com.yrw.im.rest.router;

import com.yrw.im.rest.handler.RelationHandler;
import com.yrw.im.rest.handler.UserHandler;
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
            .route(POST("/user/register").and(contentType(APPLICATION_JSON)),
                userHandler::add)
            .andRoute(POST("/user/login").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                userHandler::login)
            .andRoute(GET("/user/logout"), userHandler::logout);
    }

    @Bean
    public RouterFunction<ServerResponse> relationRoutes(RelationHandler relationHandler) {
        return RouterFunctions
            .route(GET("/relation/{id}").and(accept(APPLICATION_JSON)),
                relationHandler::listFriends)
            .andRoute(GET("/relation").and(accept(APPLICATION_JSON)),
                relationHandler::getRelation)
            .andRoute(POST("/relation").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                relationHandler::addRelation);
    }
}
