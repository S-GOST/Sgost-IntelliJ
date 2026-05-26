package com.example.sgost.model

data class CarritoItem(
    val id: String,
    val tipo: String, // "SERVICIO" o "PRODUCTO"
    val nombre: String,
    val categoria: String,
    val precioUnitario: Double,
    var cantidad: Int = 1
) {
    val subtotal: Double
        get() = precioUnitario * cantidad

    val icono: String
        get() = if (tipo == "SERVICIO") "🔧" else "⚙️"
}