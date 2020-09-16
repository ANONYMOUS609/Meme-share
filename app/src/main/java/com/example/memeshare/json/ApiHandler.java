package com.example.memeshare.json;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiHandler {

    @GET("gimme")
    Call<Meme> getMeme();
}
