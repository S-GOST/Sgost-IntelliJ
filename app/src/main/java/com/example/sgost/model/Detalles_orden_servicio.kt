package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Detalles_orden_servicio(
    // ID del detalle (usualmente auto-incremento en BD)
    @SerializedName("ID_DETALLE_ORDEN_SERVICIO") val idDetalleOrden: Int? = null,

    // Relación con la Orden
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden: Int?,

    // Relación con el Servicio
    @SerializedName("ID_SERVICIOS") val idServicios: Int?,

    // Relación con el Producto
    @SerializedName("ID_PRODUCTOS") val idProductos: Int?,

    // Atributos extra
    @SerializedName("Garantia") val garantia: Int?,
    @SerializedName("Precio") val precio: Double?
) : Parcelable