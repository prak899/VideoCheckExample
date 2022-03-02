package api.adhyay.app.videocall.RetrofitClient;


import java.util.concurrent.TimeUnit;

import api.adhyay.app.videocall.api.Api;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

     private static RetrofitClient retrofitClient;
     private static Retrofit retrofit;

     private static final String BASE_URL = "https://api.adhyay.co.in/";

    public RetrofitClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                                            .callTimeout(2, TimeUnit.MINUTES)
                                            .connectTimeout(20, TimeUnit.SECONDS)
                                            .readTimeout(30, TimeUnit.SECONDS)
                                            .writeTimeout(30, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

    }

     public static synchronized RetrofitClient getInstance(){

        if(retrofitClient== null){
            retrofitClient = new RetrofitClient();
        }

        return retrofitClient;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }
}
