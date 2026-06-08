package com.example.sgost.model

import com.google.gson.annotations.SerializedName

data class Detalles_orden_servicio(
    @SerializedName("ID_DETALLES_ORDEN_SERVICIO") val idDetalleOrden: Int? = null,
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden: Int? = null,
    @SerializedName("ID_SERVICIOS") val idServicios: Int? = null,
    @SerializedName("ID_PRODUCTOS") val idProductos: Int? = null,
    @SerializedName("NombreServicio") val nombreServicio: String? = null,
    @SerializedName("NombreProducto") val nombreProducto: String? = null,

    // ✅ CAMBIADO: Usar Double e Int directamente
    @SerializedName("Precio") val precio: Double? = 0.0,
    @SerializedName("Garantia") val garantia: Int? = 0
)