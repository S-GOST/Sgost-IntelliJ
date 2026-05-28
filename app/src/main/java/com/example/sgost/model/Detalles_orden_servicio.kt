package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Detalles_orden_servicio(
    @SerializedName("ID_DETALLES_ORDEN_SERVICIO") val idDetalle: Int? = null,
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden: Int? = null,
    @SerializedName("NombreServicio") val nombreServicio: String? = null,
    @SerializedName("NombreProducto") val nombreProducto: String? = null,
    @SerializedName("Garantia") val garantiaRaw: String? = null,
    @SerializedName("Estado") val estado: String? = null,
    @SerializedName("Precio") val precioRaw: String? = null
) : Parcelable {
    // ✅ Conversión segura String → Int/Double
    val garantia: Int get() = garantiaRaw?.toIntOrNull() ?: 0
    val precio: Double get() = precioRaw?.toDoubleOrNull() ?: 0.0
}