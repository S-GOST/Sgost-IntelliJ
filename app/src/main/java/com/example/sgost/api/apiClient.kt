package com.example.sgost.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    lateinit var apiService: ApiService

    // 🌐 Para emulador: 10.0.2.2 | 📱 Para físico: Tu IP local
    private const val BASE_URL = "http://10.0.2.2:3000/"

    fun init(context: Context) {
        // 🔍 Esto es lo nuevo: Registra todo el tráfico HTTP en Logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // Inyecta el token
            .addInterceptor(loggingInterceptor)       // 👈 AQUÍ VA EL LOG
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}