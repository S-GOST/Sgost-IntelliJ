package com.example.sgost.api

import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Cliente
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import com.example.sgost.model.Tecnico
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/admins/login")
    suspend fun loginAdmin(@Body request: LoginRequest): LoginResponse

    @GET("api/clientes")
    suspend fun obtenerClientes(@Header("Authorization") token: String): List<Cliente>

    @GET("api/tecnicos")
    suspend fun obtenerTecnicos(@Header("Authorization") token: String): List<Tecnico>

    @POST("api/tecnicos")
    suspend fun crearTecnico(@Header("Authorization") token: String, @Body tecnico: Tecnico): ApiResponse

    @PUT("api/tecnicos/{id}")
    suspend fun actualizarTecnico(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body tecnico: Tecnico
    ): ApiResponse

    @DELETE("api/tecnicos/{id}")
    suspend fun eliminarTecnico(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse
}
