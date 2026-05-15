package com.example.sgost.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 🔒 Ignorar rutas de login para evitar enviar tokens vacíos o inválidos
        if (originalRequest.url.toString().contains("/login")) {
            return chain.proceed(originalRequest)
        }

        // 1. Leer el token guardado (Clave exacta que usas en LoginActivity)
        val prefs = context.getSharedPreferences("sgost_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        // 2. Construir la petición con o sin token
        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        // 3. Ejecutar petición
        return chain.proceed(request)
    }
}