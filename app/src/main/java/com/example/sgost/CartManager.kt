package com.example.sgost

import android.content.Context
import android.content.SharedPreferences
import com.example.sgost.model.CarritoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("sgost_cart", Context.MODE_PRIVATE)
    }

    fun addItem(item: CarritoItem) {
        val current = getItems().toMutableList()
        val existing = current.find { it.id == item.id && it.tipo == item.tipo }
        if (existing != null) {
            existing.cantidad++
        } else {
            current.add(item.copy(cantidad = 1))
        }
        save(current)
    }

    fun removeAt(index: Int) {
        val current = getItems().toMutableList()
        if (index in current.indices) current.removeAt(index)
        save(current)
    }

    fun clear() = save(emptyList())

    fun getItems(): List<CarritoItem> {
        val json = prefs.getString("items", null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<CarritoItem>>() {}.type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun getItemCount(): Int = getItems().sumOf { it.cantidad }

    private fun save(items: List<CarritoItem>) {
        prefs.edit().putString("items", gson.toJson(items)).apply()
    }
}

