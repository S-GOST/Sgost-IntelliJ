package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Servicio(
    @SerializedName("ID_SERVICIOS") val idServicios: Int? = null,
    @SerializedName("Categoria") val categoria: String?,
    @SerializedName("Nombre") val nombre: String?,
    @SerializedName("Garantia") val garantiaRaw: String?, // Cambiado a String para evitar crash
    @SerializedName("Estado") val estado: String?,
    @SerializedName("Precio") val precioRaw: String?      // Cambiado a String para evitar crash
) : Parcelable {
    // Convierte seguro a Int. Retorna null si es "-", null o formato inválido
    val garantia: Int? get() = garantiaRaw?.toIntOrNull()

    // Convierte seguro a Double. Retorna null si es "-", null o formato inválido
    val precio: Double? get() = precioRaw?.toDoubleOrNull()
}