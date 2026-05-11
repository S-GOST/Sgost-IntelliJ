package com.example.sgost.api

import com.example.sgost.model.Cliente
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/admins/login")
    fun loginAdmin(@Body request: LoginRequest): Call<LoginResponse>

    // CRUD CLIENTES
    @GET("api/clientes")
    fun obtenerClientes(@Header("Authorization") token: String): Call<List<Cliente>>

    @POST("api/clientes")
    fun crearCliente(@Header("Authorization") token: String, @Body cliente: Cliente): Call<Cliente>
}