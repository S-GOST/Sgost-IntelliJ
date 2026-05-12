package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tecnico(
    @SerializedName("ID_TECNICOS")
    val id: String,

    @SerializedName("Nombre")
    val nombre: String,

    @SerializedName("usuario")
    val usuario: String,

    @SerializedName("contrasena")
    val contrasena: String,

    @SerializedName("TipoDocumento")
    val tipoDocumento: String,

    @SerializedName("Correo")
    val correo: String,

    @SerializedName("Telefono")
    val telefono: String

) : Parcelable