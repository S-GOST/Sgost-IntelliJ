package com.example.sgost

import com.example.sgost.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // ⚠️ VERIFICA: 'ApiService' debe ser el nombre exacto de la interfaz en tu archivo apiService.kt
    val authApi: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}