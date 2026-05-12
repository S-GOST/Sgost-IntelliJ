package com.example.sgost.api

import retrofit2.http.*
import com.example.sgost.model.Tecnico

data class ApiResponse(val success: Boolean, val message: String, val data: Any? = null)

interface TecnicoApiService {
    @GET("api/tecnicos")
    suspend fun getTecnicos(): List<Tecnico>

    @POST("api/tecnicos")
    suspend fun createTecnico(@Body tecnico: Tecnico): ApiResponse

    @PUT("api/tecnicos/{id}")
    suspend fun updateTecnico(@Path("id") id: String, @Body tecnico: Tecnico): ApiResponse

    @DELETE("api/tecnicos/{id}")
    suspend fun deleteTecnico(@Path("id") id: String): ApiResponse
}