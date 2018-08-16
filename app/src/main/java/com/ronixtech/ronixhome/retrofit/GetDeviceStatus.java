package com.ronixtech.ronixhome.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDeviceStatus {
    @GET("/ronix/status")
    Call<String> getStatus();
}