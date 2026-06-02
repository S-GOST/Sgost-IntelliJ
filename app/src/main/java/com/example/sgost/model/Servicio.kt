package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Servicio(
    @SerializedName("ID_SERVICIOS") val idServicios: Int? = null,
    @SerializedName("Categoria") val categoria: String?,
    @SerializedName("Nombre") val nombre: String?,
    @SerializedName("Garantia") val garantia: Int?,
    @SerializedName("Estado") val estado: String?,
    @SerializedName("Precio") val precio: Double?
) : Parcelable