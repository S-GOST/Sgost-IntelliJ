package com.example.sgost.model

data class CarritoItem(
    val idProducto: Int,
    val nombre: String,
    val precioUnitario: Double,
    var cantidad: Int,
    var subtotal: Double,
    val tipo: String,
    val icono: String = "🛠️",
    val categoria: String? = null,
    val garantia: Int? = null,
    val marca: String? = null,
    val estado: String? = null
)