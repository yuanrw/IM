package com.github.yuanrw.im.client.service;

import com.github.yuanrw.im.client.domain.UserReq;
import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
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
    @POST("/user/login")
    Call<ResultWrapper<UserInfo>> login(@Body UserReq user);

    @POST("/user/logout")
    Call<ResultWrapper<Void>> logout(@Header("token") String token);

    @GET("/relation/{id}")
    Call<ResultWrapper<List<RelationDetail>>> friends(@Path("id") String userId, @Header("token") String token);

    @GET("/relation")
    Call<ResultWrapper<RelationDetail>> relation(
        @Query("userId1") String userId1, @Query("userId2") String userId2,
        @Header("token") String token);
}
