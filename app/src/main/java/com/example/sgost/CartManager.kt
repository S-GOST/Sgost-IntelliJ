package com.example.sgost.data

import com.example.sgost.model.CarritoItem

object CartManager {
    // Lista mutable única para que Activity y Adapter compartan la misma referencia
    private val _items = mutableListOf<CarritoItem>()
    val items: List<CarritoItem> get() = _items.toList() // 🔒 Retorna copia inmutable para evitar modificaciones externas accidentales

    fun addItem(item: CarritoItem) {
        // 🔑 CORRECCIÓN CLAVE: Se compara ID Y TIPO para evitar mezclar productos y servicios con el mismo ID
        val existing = _items.find {
            it.idProducto == item.idProducto && it.tipo?.equals(item.tipo, ignoreCase = true) == true
        }

        if (existing != null) {
            // Ya existe el mismo producto/servicio: aumentamos cantidad y recalculamos subtotal
            existing.cantidad += item.cantidad
            existing.subtotal = existing.cantidad * existing.precioUnitario
        } else {
            // No existe: lo agregamos como entrada independiente
            _items.add(item)
        }
    }

    fun removeAt(index: Int) {
        if (index in _items.indices) {
            _items.removeAt(index)
        }
    }

    fun clear() {
        _items.clear()
    }

    fun total(): Double = _items.sumOf { it.subtotal }
    fun itemCount(): Int = _items.sumOf { it.cantidad }
}