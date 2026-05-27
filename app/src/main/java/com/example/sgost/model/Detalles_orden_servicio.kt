package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Detalles_orden_servicio(
    @SerializedName("ID_DETALLES_ORDEN_SERVICIO") val idDetalle: Int? = null,
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden: Int? = null,
    @SerializedName("ID_SERVICIOS") val idServicio: Int? = null,
    @SerializedName("ID_PRODUCTOS") val idProducto: Int? = null,
    @SerializedName("Garantia") val garantia: Int? = null,
    @SerializedName("Estado") val estado: String? = null,
    @SerializedName("Precio") val precio: Double? = null
) : Parcelable