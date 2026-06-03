package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    @SerializedName("ID_PRODUCTOS") val idProductos: Int? = null,
    @SerializedName("Categoria") val categoria: String?,
    @SerializedName("Marca") val marca: String?,
    @SerializedName("Nombre") val nombre: String?,
    @SerializedName("Garantia") val garantiaRaw: String?,
    @SerializedName("Precio") val precioRaw: String?,
    @SerializedName("Cantidad") val cantidad: Int?,
    @SerializedName("Estado") val estado: String?
) : Parcelable {
    val garantia: Int? get() = garantiaRaw?.toIntOrNull()
    val precio: Double? get() = precioRaw?.toDoubleOrNull()
}