package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCatalogo = findViewById<RecyclerView>(R.id.rvCatalogo)
        progressBar = findViewById(R.id.progressBar)
        rvCatalogo.layoutManager = GridLayoutManager(this, 2)

        adapter = ProductoAdapter { producto ->
            // 🔹 FLUJO 1: Botón "Agregar Producto" en el catálogo
            if (!esUsuarioLogueado()) {
                // Redirige AL LOGIN EXCLUSIVO DE CLIENTES
                startActivity(Intent(this, ClienteLoginActivity::class.java))
                return@ProductoAdapter
            }

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

        cargarProductos()

        // 🔹 FLUJO 2: Botón "Iniciar Sesión" (Barra superior) → SE MANTIENE NORMAL
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 🔹 FLUJO 3: Botón "Registrarse" → SE MANTIENE NORMAL
        findViewById<MaterialButton>(R.id.btnRegistro).setOnClickListener {
            startActivity(Intent(this, FormClienteActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val esLogueado = esUsuarioLogueado()
        Log.d(TAG, "🔍 Estado de sesión en onResume: $esLogueado")
    }

    private fun esUsuarioLogueado(): Boolean {
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        Log.d(TAG, "🔑 user_id leído de SharedPreferences: $userId")
        return userId > 0
    }

    private fun cargarProductos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val productos = ProductoRepository.obtenerProductosDesdeApi()
                adapter.actualizarLista(productos)
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando productos", e)
                Toast.makeText(this@MainActivity, "❌ Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    inner class ProductoAdapter(
        private val onAgregarClick: (Producto) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

        private var items: List<Producto> = emptyList()

        fun actualizarLista(nuevaLista: List<Producto>) {
            items = nuevaLista
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_catalogo, parent, false)
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