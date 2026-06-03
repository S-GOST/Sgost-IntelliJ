package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class Detalles_orden_servicio(
    @SerializedName("ID_DETALLES_ORDEN_SERVICIO") val idDetalleOrden: Int? = null,
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden: Int? = null,
    @SerializedName("ID_SERVICIOS") val idServicios: Int? = null,
    @SerializedName("ID_PRODUCTOS") val idProductos: Int? = null,

    // 👈 CAMPOS QUE DEVUELVE TU API
    @SerializedName("NombreServicio") val nombreServicio: String? = null,
    @SerializedName("NombreProducto") val nombreProducto: String? = null,

    @SerializedName("Precio") val precio: Double? = null,
    @SerializedName("Garantia") val garantia: Int? = null
)