package com.example.memeshare;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetMethod {

    static final String BASE_URL = "https://meme-api.herokuapp.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
