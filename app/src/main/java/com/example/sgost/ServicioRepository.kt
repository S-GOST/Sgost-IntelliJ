package com.example.sgost.data

import android.util.Log
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Servicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object ServicioRepository {
    private const val TAG = "ServicioRepository"

    suspend fun obtenerServiciosDesdeApi(): List<Servicio> = withContext(Dispatchers.IO) {
        try {
            // Realizamos la llamada a la API
            val response: Response<ApiResponse<List<Servicio>>> = ApiAndroid.apiService.obtenerServicios()

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!

                // Verificamos si la operación fue exitosa según tu backend
                if (apiResponse.success == true) {
                    return@withContext apiResponse.data ?: emptyList()
                } else {
                    // SOLUCIÓN LÍNEA 27: Quitar "msg ="
                    Log.w(TAG, "⚠️ Backend respondió success=false: ${apiResponse.message}")
                }
            } else {
                // Captura errores HTTP como 403 (Token expirado), 500, etc.
                val errorBody = response.errorBody()?.string()

                // SOLUCIÓN LÍNEA 32: Quitar "msg ="
                Log.e(TAG, "❌ Error HTTP ${response.code()}: $errorBody")
            }

            return@withContext emptyList()
        } catch (e: Exception) {
            // SOLUCIÓN LÍNEA 37: Quitar "msg =" y "tr =" y pasar la excepción 'e' directamente como 3er argumento
            Log.e(TAG, "❌ Excepción al obtener servicios", e)
            return@withContext emptyList()
        }
    }
}