package com.yrw.im.common.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.exception.ImException;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

/**
 * Date: 2019-04-21
 * Time: 16:45
 *
 * @author yrw
 */
public abstract class AbstractRestService<R> {
    private Logger logger = LoggerFactory.getLogger(AbstractRestService.class);

    protected R restClient;

    public AbstractRestService(Class<R> clazz) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080")
            .client(new OkHttpClient.Builder().addInterceptor(logging).build())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();

        this.restClient = retrofit.create(clazz);
    }

    @FunctionalInterface
    protected interface RestFunction<T> {
        /**
         * 执行一个http请求
         *
         * @return
         * @throws IOException
         */
        Response<ResultWrapper<T>> doRequest() throws IOException;
    }

    protected <T> T doRequest(RestFunction<T> function) {
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
