package com.example.sgost.model

data class CarritoItem(
    val idProducto: Int? = null,      // Cambiado a nullable
    val idServicio: Int? = null,      // 👈 NUEVO: Para guardar el ID del servicio
    val nombre: String,
    val precioUnitario: Double,
    var cantidad: Int,
    var subtotal: Double,
    val tipo: String, // "producto" o "servicio"
    val icono: String = "🛠️",
    val categoria: String? = null,
    val garantia: Int? = null,
    val marca: String? = null,
    val estado: String? = null
)