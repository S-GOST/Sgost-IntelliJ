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
import com.example.sgost.CartManager
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import org.json.JSONObject
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
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
    // ⚠️ Solo usamos prefs para datos de usuario, NO para el carrito
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        // ✅ Inicialización segura de CartManager (solo una vez en toda la app)
        if (!::adapter.isInitialized) {
            CartManager.init(applicationContext)
        }

        setupToolbar()
        initViews()
        setupAdapter()
        setupListeners()

        // 🔄 Cargar datos directamente desde CartManager
        listaCarrito = CartManager.getItems().toMutableList()
        actualizarUI()
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
            CartManager.clear()
            listaCarrito = mutableListOf()
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

    // 🔄 Métodos de gestión del carrito (Unificados con CartManager)
    fun agregarAlCarrito(item: CarritoItem) {
        CartManager.addItem(item)
        listaCarrito = CartManager.getItems().toMutableList()
        actualizarUI()
    }

    private fun eliminarItem(index: Int) {
        CartManager.removeAt(index)
        listaCarrito = CartManager.getItems().toMutableList()
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

    override fun onResume() {
        super.onResume()
        // Sincronización automática al volver de otras pantallas
        listaCarrito = CartManager.getItems().toMutableList()
        actualizarUI()
    }

    // 🌐 Llamada a API (Optimizada y segura)
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

                val ordenServicio = Orden_servicio(
                    idOrden_servicio = null,
                    idClientes = userId,
                    idAdministrador = null,
                    idTecnicos = null,
                    idMotos = null,
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = null,
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                val response: Response<ResponseBody> = ApiAndroid.apiService.crearOrdenServicio(ordenServicio)

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonString)
                    val success = jsonObject.optBoolean("success", false)

                    if (success) {
                        showToast("✅ Orden generada correctamente")
                        CartManager.clear() // Vaciar de forma centralizada
                        listaCarrito = mutableListOf()
                        actualizarUI()
                        finish()
                    } else {
                        showToast("❌ ${jsonObject.optString("message", "Error desconocido")}")
                    }
                } else {
                    val errorString = response.errorBody()?.string() ?: "Error de conexión"
                    val errorMsg = try {
                        JSONObject(errorString).optString("message") ?: JSONObject(errorString).optString("error")
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