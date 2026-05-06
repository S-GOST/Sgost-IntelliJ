package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("contrasena") val contrasena: String
)