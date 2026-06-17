package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    @SerializedName("ID_PRODUCTOS") val idProductos: Int? = null,
    @SerializedName("Categoria") val categoria: String? = null,
    @SerializedName("Marca") val marca: String? = null,
    @SerializedName("Nombre") val nombre: String? = null,
    @SerializedName("Garantia") val garantiaRaw: String? = null,
    @SerializedName("Precio") val precioRaw: String? = null,
    @SerializedName("Cantidad") val cantidad: Int? = null,
    @SerializedName("Estado") val estado: String? = null,
    // Campo extra para diferenciar entre Producto y Servicio (no viene de la API)
    val tipo: String = "Producto"  // "Producto" o "Servicio"
) : Parcelable {
    val garantia: Int? get() = garantiaRaw?.toIntOrNull()
    val precio: Double? get() = precioRaw?.toDoubleOrNull()
}