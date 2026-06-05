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
import com.example.sgost.model.ApiResponse
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONObject
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
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
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
    fun agregarAlCarrito(item: CarritoItem) {
        val existente = listaCarrito.find { it.id == item.id && it.tipo == item.tipo }
        if (existente != null) {
            existente.cantidad++
        } else {
            listaCarrito.add(item)
        }
        guardarCarritoEnAlmacen()
        actualizarUI()
    }

    private fun eliminarItem(index: Int) {
        if (index in listaCarrito.indices) {
            listaCarrito.removeAt(index)
            guardarCarritoEnAlmacen()
            actualizarUI()
        }
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
        try {
            val json = Gson().toJson(listaCarrito)
            prefs.edit().putString("carrito_items", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cargarCarritoDesdeAlmacen() {
        try {
            val json = prefs.getString("carrito_items", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<CarritoItem>>() {}.type
                listaCarrito.clear()
                listaCarrito.addAll(Gson().fromJson(json, type) ?: emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            actualizarUI()
        }
    }

    // 🌐 Llamada a API (CORREGIDA Y COMPILABLE)
    // 🌐 Llamada a API (CORREGIDA PARA USAR ResponseBody)
    private fun generarOrdenServicio() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    showToast("⚠️ Conexión API no disponible")
                    return@launch
                }

                val userId = prefs.getInt("user_id", 0)
                if (userId == 0) {
                    showToast("❌ Error: Usuario no autenticado o ID inválido")
                    return@launch
                }

                // 📦 Construir el objeto Orden
                val ordenServicio = Orden_servicio(
                    idOrden_servicio = null,
                    idClientes = userId,
                    idAdministrador = null, // ⚠️ Importante: Tu base de datos requiere este ID (visto en logs)
                    idTecnicos = null,      // ⚠️ Importante: Tu base de datos requiere este ID (visto en logs)
                    idMotos = null,      // Si es obligatorio, necesitas pasar el ID de la moto aquí
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = null,
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                // 🔽 Llamada directa (Ahora devuelve ResponseBody, que es texto crudo)
                val response: Response<ResponseBody> = ApiAndroid.apiService.crearOrdenServicio(ordenServicio)

                // ✅ Manejo manual de la respuesta JSON
                if (response.isSuccessful) {
                    // 1. Convertir la respuesta a String
                    val jsonString = response.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonString)

                    // 2. Leer el campo 'success' del JSON
                    val success = jsonObject.optBoolean("success", false)

                    if (success) {
                        showToast("✅ Orden generada correctamente")
                        listaCarrito.clear()
                        guardarCarritoEnAlmacen()
                        actualizarUI()
                        finish()
                    } else {
                        val errorMsg = jsonObject.optString("message", "Error desconocido")
                        showToast("❌ $errorMsg")
                    }

                } else {
                    // 3. Error de red (Códigos 4xx o 5xx)
                    val errorString = response.errorBody()?.string() ?: "Error de conexión"

                    // Intentar leer el mensaje de error del servidor si existe
                    val errorMsg = try {
                        val errorJson = JSONObject(errorString)
                        errorJson.optString("message") ?: errorJson.optString("error")
                    } catch (e: Exception) {
                        errorString
                    }
                    showToast("❌ $errorMsg")
                }

            } catch (e: Exception) {
                e.printStackTrace()
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