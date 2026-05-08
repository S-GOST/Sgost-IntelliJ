package com.example.sgost

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarritoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Cargar el diseño sin ViewBinding
        setContentView(R.layout.activity_carrito)

        // 2. Referenciar elementos
        val rvCarrito = findViewById<RecyclerView>(R.id.rvCarrito)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        // 3. Configurar RecyclerView
        rvCarrito.layoutManager = LinearLayoutManager(this)
        // Aquí asignarías el adapter cuando lo tengas

        // 4. Ejemplo de totales
        tvSubtotal.text = "$ 1,250.00"
        tvTotal.text = "$ 1,250.00"

        // 5. Botón Checkout
        btnCheckout.setOnClickListener {
            // Lógica de checkout
        }
    }
}