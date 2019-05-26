package com.yrw.im.common.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.exception.ImException;
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
            //            .client(new OkHttpClient.Builder().addInterceptor(logging).build())
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
                throw new ImException("[rest service] status is not 200, response body: " + response.errorBody());
            }
            if (response.body() == null) {
                throw new ImException("[rest service] response body is null");
            }
            if (response.body().getStatus() != 200) {
                throw new ImException("[rest service] status is not 200, response body: " + new ObjectMapper().writeValueAsString(response.body()));
            }
            return response.body().getData();
        } catch (IOException e) {
            throw new ImException("[rest service] has error", e);
        }
    }
}
