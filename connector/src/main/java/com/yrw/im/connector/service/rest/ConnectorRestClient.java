package com.yrw.im.connector.service.rest;

import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.domain.po.Offline;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * Date: 2019-05-28
 * Time: 00:18
 *
 * @author yrw
 */
public interface ConnectorRestClient {

    @GET("/offline/poll")
    Call<ResultWrapper<List<Offline>>> pollOfflineMsg(@Query("userId") Long userId);
}
