package com.vo.adertechaudioapp_v1.adertech;

import com.vo.adertechaudioapp_v1.adertech.StreamerApi.GetPlayerStatus;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HTTPSSocket {
    private static final String TAG = HTTPSSocket.class.getSimpleName();

    private static HTTPSSocket mInctance;
    private static String baseURL;
    private Retrofit retrofit;

    StreamerApiService streamerApiService;

    public HTTPSSocket(String ipAddress) {
        baseURL = "https://" + ipAddress + ":8443/";

        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static HTTPSSocket getInstance() {
        if (mInctance == null) {
            mInctance = new HTTPSSocket("10.0.3.112");
        }
        return mInctance;
    }

    public StreamerApiService getJSONApi() {
        return retrofit.create(StreamerApiService.class);
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
}
