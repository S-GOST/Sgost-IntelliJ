package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.CarritoAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    // 🔹 Vistas
    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvItemCount: android.widget.TextView
    private lateinit var llEmptyState: android.widget.LinearLayout
    private lateinit var tvSubtotal: android.widget.TextView
    private lateinit var tvTotal: android.widget.TextView
    private lateinit var btnGenerarOrden: MaterialButton
    private lateinit var btnVaciar: android.widget.ImageButton

    // 🔹 Estado del carrito
    private var listaCarrito = mutableListOf<CarritoItem>()
    private lateinit var adapter: CarritoAdapter

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        setupToolbar()
        initViews()
        setupAdapter()
        setupListeners()

        cargarCarritoDesdeAlmacen()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        rvCarrito = findViewById(R.id.rvCarrito)
        tvItemCount = findViewById(R.id.tvItemCount)
        llEmptyState = findViewById(R.id.llEmptyState)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        btnGenerarOrden = findViewById(R.id.btnGenerarOrden)
        btnVaciar = findViewById(R.id.btnVaciar)
    }

    private fun setupAdapter() {
        rvCarrito.isNestedScrollingEnabled = false
        rvCarrito.layoutManager = LinearLayoutManager(this)
        adapter = CarritoAdapter(listaCarrito) { index -> eliminarItem(index) }
        rvCarrito.adapter = adapter
    }

    private fun setupListeners() {
        btnVaciar.setOnClickListener {
            if (listaCarrito.isEmpty()) return@setOnClickListener
            listaCarrito.clear()
            guardarCarritoEnAlmacen()
            actualizarUI()
        }

        btnGenerarOrden.setOnClickListener {
            if (listaCarrito.isEmpty()) {
                showToast("❌ El carrito está vacío")
                return@setOnClickListener
            }
            btnGenerarOrden.isEnabled = false
            btnGenerarOrden.text = "PROCESANDO..."
            generarOrdenServicio()
        }
    }

    // 🔄 Métodos de gestión del carrito
    private fun agregarAlCarrito(item: CarritoItem) {
        val existente = listaCarrito.find { it.id == item.id && it.tipo == item.tipo }
        if (existente != null) existente.cantidad++ else listaCarrito.add(item)
        guardarCarritoEnAlmacen()
        actualizarUI()
    }

    private fun eliminarItem(index: Int) {
        listaCarrito.removeAt(index)
        guardarCarritoEnAlmacen()
        actualizarUI()
    }

    private fun actualizarUI() {
        adapter.notifyDataSetChanged()
        val totalItems = listaCarrito.sumOf { it.cantidad }
        tvItemCount.text = "$totalItems artículo${if (totalItems != 1) "s" else ""} en el carrito"

        llEmptyState.visibility = if (listaCarrito.isEmpty()) View.VISIBLE else View.GONE
        rvCarrito.visibility = if (listaCarrito.isEmpty()) View.GONE else View.VISIBLE
        calcularTotales()
    }

    private fun calcularTotales() {
        val total = listaCarrito.sumOf { it.subtotal }
        tvSubtotal.text = formatoMoneda.format(total)
        tvTotal.text = formatoMoneda.format(total)
    }

    // 💾 Persistencia
    private fun guardarCarritoEnAlmacen() {
        val json = com.google.gson.Gson().toJson(listaCarrito)
        prefs.edit().putString("carrito_items", json).apply()
    }

    private fun cargarCarritoDesdeAlmacen() {
        val json = prefs.getString("carrito_items", null)
        if (!json.isNullOrEmpty()) {
            val type = object : com.google.gson.reflect.TypeToken<List<CarritoItem>>() {}.type
            listaCarrito.clear()
            listaCarrito.addAll(com.google.gson.Gson().fromJson(json, type))
        }
        actualizarUI()
    }

    // 🌐 Llamada a API (Simplificada y corregida)
    private fun generarOrdenServicio() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    showToast("⚠️ Conexión API no disponible")
                    return@launch
                }

                // 📦 Construir el objeto exacto que espera tu backend
                val ordenServicio = Orden_servicio(
                    idOrden_servicio = null, // El backend genera el ID
                    idClientes = prefs.getInt("user_id", 0), // 👈 Aquí va el ID del cliente logueado
                    idAdministrador = null, // Asigna si tu backend lo requiere
                    idTecnicos = null,      // Asigna si tu backend lo requiere
                    idMotos = null,         // 👈 Aquí deberías pasar el ID de la moto seleccionada
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = null,
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                // 🔽 Llamada directa a tu endpoint
                val response = ApiAndroid.apiService.crearOrdenServicio(ordenServicio)

                // ✅ Manejo correcto de Response<ApiResponse<...>>
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("✅ Orden generada correctamente")
                    listaCarrito.clear()
                    guardarCarritoEnAlmacen()
                    actualizarUI()
                    finish() // Vuelve al dashboard
                } else {
                    val errorMsg = response.body()?.message ?: "Error al crear la orden"
                    showToast("❌ $errorMsg")
                }

            } catch (e: Exception) {
                showToast("❌ Error de red: ${e.message}")
            } finally {
                btnGenerarOrden.isEnabled = true
                btnGenerarOrden.text = "GENERAR ORDEN DE SERVICIO"
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}