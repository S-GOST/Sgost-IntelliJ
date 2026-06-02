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
    @SerializedName("Garantia") val garantia: Int?,
    @SerializedName("Precio") val precio: Double?,
    @SerializedName("Cantidad") val cantidad: Int?,
    @SerializedName("Estado") val estado: String?
) : Parcelable