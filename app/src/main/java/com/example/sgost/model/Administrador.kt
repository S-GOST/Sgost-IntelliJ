package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Administrador(
    @SerializedName("ID_ADMINISTRADOR") val id: Int? = null,
    @SerializedName("Nombre") val nombre: String? = null,
    @SerializedName("usuario") val usuario: String? = null,
    @SerializedName("contrasena") val contrasena: String? = null,
    @SerializedName("Correo") val correo: String? = null,
    @SerializedName("TipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("Telefono") val telefono: String? = null
) : Parcelable