package com.yim.im.client.service;

import com.yim.im.client.domain.UserReq;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.domain.po.Relation;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 17:03
 *
 * @author yrw
 */
public interface ClientRestClient {

    @Headers("Content-Type: application/json")
    @POST("/user/register")
    Call<ResultWrapper<Long>> register(@Body UserReq user);

    @Headers("Content-Type: application/json")
    @POST("/user/online")
    Call<ResultWrapper<UserInfo>> login(@Body UserReq user);

    @POST("/user/logout")
    Call<ResultWrapper<Void>> logout(@Header("token") String token);

    @GET("/relation/{id}")
    Call<ResultWrapper<List<Relation>>> friends(@Path("id") Long userId, @Header("token") String token);

    @GET("/relation")
    Call<ResultWrapper<Relation>> relation(
        @Query("userId1") Long userId1, @Query("userId2") Long userId2,
        @Header("token") String token);
}
