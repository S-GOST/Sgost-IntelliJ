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
import com.example.sgost.ProductoRepository
import com.example.sgost.data.ServicioRepository // 👈 Asegúrate de tener este archivo
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Producto
import com.example.sgost.model.Servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: CatalogAdapter
    private lateinit var progressBar: ProgressBar
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCatalogo = findViewById<RecyclerView>(R.id.rvCatalogo)
        progressBar = findViewById(R.id.progressBar)
        rvCatalogo.layoutManager = GridLayoutManager(this, 2)

        // 🔹 INICIALIZAR ADAPTADOR MEZCLADO (Productos + Servicios)
        adapter = CatalogAdapter { item ->
            if (!esUsuarioLogueado()) {
                startActivity(Intent(this, ClienteLoginActivity::class.java))
                return@CatalogAdapter
            }

            // 👇 SEPARACIÓN EXPLÍCITA: Cada tipo crea su propio CarritoItem independiente
            when (item) {
                is Producto -> {
                    val carritoItem = CarritoItem(
                        idProducto = item.idProductos,
                        idServicio = null, // 🔒 Nunca llevará servicio
                        nombre = item.nombre ?: "Producto",
                        precioUnitario = item.precio ?: 0.0,
                        cantidad = 1,
                        subtotal = item.precio ?: 0.0,
                        tipo = "producto",
                        garantia = item.garantia,
                        categoria = item.categoria,
                        marca = item.marca
                    )
                    CartManager.addItem(carritoItem)
                    Log.d(TAG, "🛒 PRODUCTO agregado -> ID: ${carritoItem.idProducto} | Nombre: ${carritoItem.nombre}")
                    Toast.makeText(this, "✅ Producto agregado: ${carritoItem.nombre}", Toast.LENGTH_SHORT).show()
                }
                is Servicio -> {
                    val carritoItem = CarritoItem(
                        idProducto = null, // 🔒 Nunca llevará producto
                        idServicio = item.idServicios,
                        nombre = item.nombre ?: "Servicio",
                        precioUnitario = item.precio ?: 0.0,
                        cantidad = 1,
                        subtotal = item.precio ?: 0.0,
                        tipo = "servicio",
                        garantia = item.garantia,
                        categoria = item.categoria,
                        marca = null
                    )
                    CartManager.addItem(carritoItem)
                    Log.d(TAG, "🛒 SERVICIO agregado -> ID: ${carritoItem.idServicio} | Nombre: ${carritoItem.nombre}")
                    Toast.makeText(this, "✅ Servicio agregado: ${carritoItem.nombre}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvCatalogo.adapter = adapter

        cargarCatalogoMixto()

        // 🔹 BOTONES DE NAVEGACIÓN
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnRegistro).setOnClickListener {
            startActivity(Intent(this, FormClienteActivity::class.java))
        }
        findViewById<View>(R.id.ivCarrito).setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔍 Estado de sesión: ${esUsuarioLogueado()}")
    }

    private fun esUsuarioLogueado(): Boolean {
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        return userId > 0
    }

    // 🔹 CARGAR PRODUCTOS Y SERVICIOS JUNTOS (SIN DUPLICADOS)
    private fun cargarCatalogoMixto() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // ✅ CORRECCIÓN CLAVE: Llamar a REPOSITORIOS DISTINTOS
                val listaProductos = ProductoRepository.obtenerProductosDesdeApi()
                val listaServicios = ServicioRepository.obtenerServiciosDesdeApi() // 👈 Endpoint correcto

                val listaMixta = mutableListOf<Any>()
                listaMixta.addAll(listaProductos)
                listaMixta.addAll(listaServicios)

                adapter.actualizarLista(listaMixta)
                Log.d(TAG, "📦 Catálogo cargado: ${listaProductos.size} productos + ${listaServicios.size} servicios")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando catálogo mixto", e)
                Toast.makeText(this@MainActivity, "❌ Error al cargar catálogo", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // 🔹 ADAPTADOR UNIVERSAL
    inner class CatalogAdapter(
        private val onAgregarClick: (Any) -> Unit
    ) : RecyclerView.Adapter<CatalogAdapter.ViewHolder>() {

        private var items: List<Any> = emptyList()

        fun actualizarLista(nuevaLista: List<Any>) {
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

            fun bind(item: Any, clickListener: (Any) -> Unit) {
                var icono = ""
                var nombre = ""
                var precio = 0.0
                var desc = ""

                when (item) {
                    is Producto -> {
                        icono = "🛍️"
                        nombre = item.nombre ?: "Sin nombre"
                        precio = item.precio ?: 0.0
                        desc = when {
                            item.categoria != null && item.marca != null -> "${item.categoria} • ${item.marca}"
                            item.categoria != null -> item.categoria
                            else -> ""
                        }
                    }
                    is Servicio -> {
                        icono = "🔧"
                        nombre = item.nombre ?: "Sin nombre"
                        precio = item.precio ?: 0.0
                        desc = item.categoria ?: ""
                    }
                    else -> {
                        icono = "❓"
                        nombre = "Desconocido"
                    }
                }

                tvIcono.text = icono
                tvNombre.text = nombre
                tvDescripcion.text = desc
                tvPrecio.text = "$${String.format("%.2f", precio)}"
                btnAgregar.setOnClickListener { clickListener(item) }
            }
        }
    }
}