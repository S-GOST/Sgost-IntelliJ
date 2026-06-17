package com.example.sgost.data

import com.example.sgost.model.CarritoItem

object CartManager {
    private val _items = mutableListOf<CarritoItem>()
    val items: List<CarritoItem> get() = _items

    fun addItem(item: CarritoItem) {
        val existing = _items.find { it.idProducto == item.idProducto }
        if (existing != null) {
            existing.cantidad += item.cantidad
            existing.subtotal = existing.cantidad * existing.precioUnitario
        } else {
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
}