package com.example.sgost.api

import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Cliente
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import com.example.sgost.model.Tecnico
import com.example.sgost.model.RegistroRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 🔐 LOGIN
    @POST("api/admins/login")
    suspend fun loginAdmin(@Body request: LoginRequest): LoginResponse

    @POST("api/tecnicos/login")
    suspend fun loginTecnico(@Body request: LoginRequest): LoginResponse

    @POST("api/clientes/login")
    suspend fun loginCliente(@Body request: LoginRequest): LoginResponse

    //---------------------------------------------------------------------------------------------------------------------------------//

    // TÉCNICOS

    @POST("api/tecnicos/insertar")
    suspend fun crearTecnico(@Body tecnico: Tecnico): ApiResponse<Tecnico>

    @GET("api/tecnicos/obtener")
    suspend fun obtenerTecnicos(): ApiResponse<List<Tecnico>>

    @GET("api/tecnicos/buscar/{id}")
    suspend fun obtenertecnicosPorId(@Path("id") id: String): Response<ApiResponse<Tecnico>>

    @PUT("api/tecnicos/actualizar/{id}")
    suspend fun actualizarTecnico(
        @Path("id") id: String,
        @Body tecnico: Tecnico
    ): ApiResponse<Tecnico>

    @DELETE("api/tecnicos/eliminar/{id}")
    suspend fun eliminarTecnico(@Path("id") id: String): ApiResponse<Unit>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // CLIENTES
    @POST("api/clientes/insertar")
    suspend fun registrarCliente(@Body cliente: Cliente): Response<ApiResponse<Cliente>>

    @GET("api/clientes/obtener")
    suspend fun obtenerClientes(): ApiResponse<List<Cliente>>

    @GET("api/clientes/buscar/{id}")
    suspend fun obtenerClientePorId(@Path("id") id: String): Response<ApiResponse<Cliente>>

    @PUT("api/clientes/actualizar/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: String,
        @Body cliente: Cliente
    ): Response<ApiResponse<Cliente>> // 👈 CAMBIO AQUI

    @DELETE("api/clientes/eliminar/{id}")
    suspend fun eliminarCliente(@Path("id") id: String): Response<ApiResponse<Unit>>
}