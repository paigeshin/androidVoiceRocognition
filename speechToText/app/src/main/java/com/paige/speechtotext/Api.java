package com.paige.speechtotext;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @FormUrlEncoded
    @POST("/")
    Call<Message> sendMessage(@Field("message") String message);

}
