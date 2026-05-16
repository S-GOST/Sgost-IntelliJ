package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("correo") val correo: String? = null,      // ← minúscula
    @SerializedName("telefono") val telefono: String? = null,  // ← minúscula
    @SerializedName("ubicacion") val ubicacion: String? = null // ← minúscula
)