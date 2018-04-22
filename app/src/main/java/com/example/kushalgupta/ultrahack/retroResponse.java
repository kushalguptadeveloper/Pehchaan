package com.example.kushalgupta.ultrahack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.kushalgupta.ultrahack.models.s3;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class retroResponse extends AppCompatActivity {
Retrofit retrofit;
ApiInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retro_response);


        retrofit = new Retrofit.Builder()
                .baseUrl("https://s3.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface=retrofit.create(ApiInterface.class);
        Call<s3> call = apiInterface.gets3();
        call.enqueue(new Callback<s3>() {
            @Override
            public void onResponse(Call<s3> call, Response<s3> response) {
                s3 obj=response.body();
                Toast.makeText(retroResponse.this, ""+obj.getConfidence().getS(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<s3> call, Throwable t) {

            }
        });
    }
}
