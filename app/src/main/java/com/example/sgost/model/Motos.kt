package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Moto(
    @SerializedName("ID_MOTOS") val idMotos: Int? = null,
    @SerializedName("ID_CLIENTES") val idClientes: Int? = null,
    @SerializedName("Placa") val placa: String?,
    @SerializedName("Modelo") val modelo: String,      // Es Int porque en tu base de datos son números (1290, 250)
    @SerializedName("Marca") val marca: String,
    @SerializedName("Recorrido") val recorrido: Double? // Double para manejar valores como 80.000
) : Parcelable