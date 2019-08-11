package com.github.yuanrw.im.rest.web.filter;

import com.github.yuanrw.im.common.exception.ImException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Date: 2019-04-21
 * Time: 15:51
 *
 * @author yrw
 */
@Component
public class HeaderFilter implements WebFilter {

    private TokenManager tokenManager;

    public HeaderFilter(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        String path = serverWebExchange.getRequest().getPath().value();

        if ("/user/login".equals(path) || path.startsWith("/offline")) {
            return webFilterChain.filter(serverWebExchange);
        }
        if (!serverWebExchange.getRequest().getHeaders().containsKey("token")) {
            return Mono.error(new ImException("[rest] user is not login"));
        }

        String token = serverWebExchange.getRequest().getHeaders().getFirst("token");

        return tokenManager.validateToken(token).flatMap(b -> b != null ? webFilterChain.filter(serverWebExchange) :
            Mono.error(new ImException("[rest] user is not login")));
    }
}
