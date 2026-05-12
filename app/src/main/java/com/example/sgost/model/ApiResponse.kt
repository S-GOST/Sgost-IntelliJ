package com.example.sgost.model

import com.google.gson.annotations.SerializedName

// Este es el modelo estándar para recibir respuestas de éxito o error de la API
data class ApiResponse(
    // Mapea la clave "success" del JSON a la variable booleana success
    @SerializedName("success")
    val success: Boolean,

    // Mapea la clave "message" del JSON a la variable de texto message
    @SerializedName("message")
    val message: String
)