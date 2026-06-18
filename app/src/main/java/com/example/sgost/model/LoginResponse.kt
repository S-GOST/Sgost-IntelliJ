package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("token") val token: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("rol") val rol: String?,
    @SerializedName("mensaje") val message: String? = null
)