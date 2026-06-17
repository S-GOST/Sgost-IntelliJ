package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.data.CartManager
import com.example.sgost.data.ProductoRepository
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Producto
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCatalogo = findViewById<RecyclerView>(R.id.rvCatalogo)
        progressBar = findViewById(R.id.progressBar) // Añade un ProgressBar en tu layout
        rvCatalogo.layoutManager = GridLayoutManager(this, 2)

        adapter = ProductoAdapter { producto ->
            val item = CarritoItem(
                idProducto = producto.idProductos ?: 0,
                nombre = producto.nombre ?: "Sin nombre",
                precioUnitario = producto.precio ?: 0.0,
                cantidad = 1,
                subtotal = producto.precio ?: 0.0,
                tipo = producto.tipo,
                icono = if (producto.tipo.equals("Servicio", ignoreCase = true)) "🔧" else "🛠️",
                categoria = producto.categoria,
                garantia = producto.garantia,
                marca = producto.marca,
                estado = producto.estado
            )
            CartManager.addItem(item)
            Toast.makeText(this, "✅ Agregado: ${producto.nombre}", Toast.LENGTH_SHORT).show()
        }
        rvCatalogo.adapter = adapter

        // Cargar productos desde la API
        cargarProductos()

        // Botones
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnRegistro).setOnClickListener {
            startActivity(Intent(this, FormClienteActivity::class.java))
        }
    }

    private fun cargarProductos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val productos = ProductoRepository.obtenerProductosDesdeApi()
            adapter.actualizarLista(productos)
            progressBar.visibility = View.GONE
        }
    }

    // Adaptador interno (sin cambios, solo para referencia)
    inner class ProductoAdapter(
        private val onAgregarClick: (Producto) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

        private var items: List<Producto> = emptyList()

        fun actualizarLista(nuevaLista: List<Producto>) {
            items = nuevaLista
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_carrito, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], onAgregarClick)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvIcono = itemView.findViewById<TextView>(R.id.tvIcono)
            private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombre)
            private val tvDescripcion = itemView.findViewById<TextView>(R.id.tvDescripcion)
            private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecio)
            private val btnAgregar = itemView.findViewById<MaterialButton>(R.id.btnAgregar)

            fun bind(producto: Producto, clickListener: (Producto) -> Unit) {
                tvIcono.text = if (producto.tipo.equals("Servicio", ignoreCase = true)) "🔧" else "🛠️"
                tvNombre.text = producto.nombre ?: "Sin nombre"
                val desc = when {
                    producto.categoria != null && producto.marca != null -> "${producto.categoria} • ${producto.marca}"
                    producto.categoria != null -> producto.categoria
                    producto.marca != null -> producto.marca
                    else -> ""
                }
                tvDescripcion.text = desc
                tvPrecio.text = "$${String.format("%.2f", producto.precio ?: 0.0)}"
                btnAgregar.setOnClickListener { clickListener(producto) }
            }
        }
    }
}