package com.yim.im.client.service;

import com.yim.im.client.domain.UserReq;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.UserInfo;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 17:03
 *
 * @author yrw
 */
public interface RestClient {

    @Headers("Content-Type: application/json")
    @POST("/user/register")
    Call<ResultWrapper<Long>> register(@Body UserReq user);

    @Headers("Content-Type: application/json")
    @POST("/user/login")
    Call<ResultWrapper<UserInfo>> login(@Body UserReq user);

    @POST("/user/logout")
    Call<ResultWrapper<Void>> logout(@Header("token") String token);

    @GET("/relation/{id}")
    Call<ResultWrapper<List<Relation>>> friends(@Path("id") Long userId, @Header("token") String token);
}
