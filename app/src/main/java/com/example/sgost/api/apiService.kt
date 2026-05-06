package com.example.sgost.api

import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/admins/login")
    fun loginAdmin(@Body request: LoginRequest): Call<LoginResponse>
}