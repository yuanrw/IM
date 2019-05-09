package com.yrw.im.common.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yim.im.client.domain.UserReq;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.exception.ImException;
import io.netty.util.CharsetUtil;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 16:45
 *
 * @author yrw
 */
public class RestService {

    private Logger logger = LoggerFactory.getLogger(RestService.class);

    private RestClient restClient;

    public RestService() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080")
            .client(new OkHttpClient.Builder().addInterceptor(logging).build())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();

        this.restClient = retrofit.create(RestClient.class);
    }

    public UserInfo login(String username, String password) {
        return doRequest(() ->
            restClient.login(new UserReq(username, pwdSha256(password))).execute());
    }

    public Void logout(String token) {
        return doRequest(() -> restClient.logout(token).execute());
    }

    public List<Relation> friends(Long userId, String token) {
        return doRequest(() -> restClient.friends(userId, token).execute());
    }

    private String pwdSha256(String password) {
        return DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8));
    }

    @FunctionalInterface
    private interface RestFunction<T> {
        /**
         * 执行一个http请求
         *
         * @return
         * @throws IOException
         */
        Response<ResultWrapper<T>> doRequest() throws IOException;
    }

    private <T> T doRequest(RestFunction<T> function) {
        try {
            Response<ResultWrapper<T>> response = function.doRequest();
            if (!response.isSuccessful()) {
                logger.error("[rest service] rest error: ", response.errorBody());
                throw new ImException("fail");
            }
            if (response.body() == null) {
                logger.error("[rest service] rest response body is null");
                throw new ImException("fail");
            }
            if (response.body().getStatus() != 200) {
                logger.error("[rest service] rest error: {}", new ObjectMapper().writeValueAsString(response.body()));
                throw new ImException("fail");
            }
            return response.body().getData();
        } catch (IOException e) {
            logger.error("[rest service] rest error: ", e);
            throw new ImException("fail");
        }
    }
}
