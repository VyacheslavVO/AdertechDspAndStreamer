package com.vo.adertechaudioapp_v1.adertech;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface StreamerApiService {

    @GET
    Call<ResponseBody> executeGet(@Url String fullUrl);
    @POST
    Call<ResponseBody> executePost(@Url String fullUrl, @Body RequestBody body);
}
