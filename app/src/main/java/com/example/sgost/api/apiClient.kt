package com.example.sgost.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://192.168.112.1/api/"

     val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 👈 Esto es lo que te faltaba para conectar con la Activity
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}