package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("ID_CLIENTES") val id: Int? = null, // 👈 Si tu backend envía "ID_CLIENTES", cámbialo aquí
    @SerializedName("Ubicacion") val ubicacion: String? = null,
    @SerializedName("Nombre") val nombre: String? = null,
    @SerializedName("usuario") val usuario: String? = null,
    @SerializedName("contrasena") val contrasena: String? = null, // 🔒 Solo para POST/PUT. Nunca expongas en GET
    @SerializedName("TipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("Correo") val correo: String? = null,
    @SerializedName("Telefono") val telefono: String? = null
)