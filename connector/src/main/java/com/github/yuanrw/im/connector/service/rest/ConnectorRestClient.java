package com.github.yuanrw.im.connector.service.rest;

import com.github.yuanrw.im.common.domain.ResultWrapper;
import com.github.yuanrw.im.common.domain.po.Offline;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * Date: 2019-05-28
 * Time: 00:18
 *
 * @author yrw
 */
public interface ConnectorRestClient {

    @GET("/offline/poll/{id}")
    Call<ResultWrapper<List<Offline>>> pollOfflineMsg(@Path("id") String userId);
}
