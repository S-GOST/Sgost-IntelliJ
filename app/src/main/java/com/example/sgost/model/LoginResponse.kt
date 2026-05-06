package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("token") val token: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("rol") val rol: String = "",
    @SerializedName("message") val message: String = ""
)