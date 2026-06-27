package com.example.sgost

import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Producto

object ProductoRepository {
    suspend fun obtenerProductosDesdeApi(): List<Producto> {
        var productos = mutableListOf<Producto>()

        try {
            // 1. Obtienes el Response de Retrofit
            val responseHttp = ApiAndroid.apiService.obtenerProductos()

            // 2. Extraes el cuerpo (tu ApiResponse<List<Producto>>)
            val responseBody = responseHttp.body()

            // 3. Ahora sí puedes acceder a .success y .data
            if (responseBody?.success == true) {
                responseBody.data?.let { lista ->
                    productos.addAll(lista)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return productos
    }
}