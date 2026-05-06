package com.example.sgost.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // 1. Intentar leer el token de SharedPreferences
        val prefs = context.getSharedPreferences("sgost_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        // 2. Si hay token, agregamos el header de autorización
        if (!token.isNullOrEmpty()) {
            request = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        // 3. Continuar con la petición
        return chain.proceed(request)
    }
}