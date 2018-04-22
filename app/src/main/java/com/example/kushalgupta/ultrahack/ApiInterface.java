package com.example.kushalgupta.ultrahack;

import com.example.kushalgupta.ultrahack.models.s3;

import retrofit2.Call;
import retrofit2.http.POST;

public interface ApiInterface {

@POST("mausamrest/data.json")
    Call<s3>  gets3();
}
