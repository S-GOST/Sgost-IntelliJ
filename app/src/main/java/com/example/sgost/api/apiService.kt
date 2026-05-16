package com.example.sgost.api

import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Cliente
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import com.example.sgost.model.Tecnico
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 🔐 AUTENTICACIÓN
    @POST("api/admins/login")
    suspend fun loginAdmin(@Body request: LoginRequest): LoginResponse

    @POST("api/tecnicos/login")
    suspend fun loginTecnico(@Body request: LoginRequest): LoginResponse

    @POST("api/clientes/login")
    suspend fun loginCliente(@Body request: LoginRequest): LoginResponse // ✅ Corregido: nombre coincide con LoginActivityAdmin

    // 👥 GESTIÓN DE CLIENTES
    @GET("api/clientes")
    suspend fun obtenerClientes(): List<Cliente>

    @GET("api/clientes/{id}")
    suspend fun obtenerClientePorId(@Path("id") id: String): Cliente

    @POST("api/clientes")
    suspend fun crearCliente(@Body cliente: Cliente): ApiResponse

    @PUT("api/clientes/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: String,
        @Body cliente: Cliente
    ): ApiResponse

    @DELETE("api/clientes/{id}")
    suspend fun eliminarCliente(
        @Path("id") id: String
    ): ApiResponse

    // 🔧 GESTIÓN DE TÉCNICOS
    @GET("api/tecnicos")
    suspend fun obtenerTecnicos(): List<Tecnico>

    @POST("api/tecnicos")
    suspend fun crearTecnico(@Body tecnico: Tecnico): ApiResponse

    @PUT("api/tecnicos/{id}")
    suspend fun actualizarTecnico(
        @Path("id") id: String,
        @Body tecnico: Tecnico
    ): ApiResponse

    @DELETE("api/tecnicos/{id}")
    suspend fun eliminarTecnico(
        @Path("id") id: String
    ): ApiResponse
}