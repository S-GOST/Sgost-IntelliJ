package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.CarritoAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.data.CartManager
import com.example.sgost.model.CarritoItem
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    // ==========================================
    // 1. VISTAS Y DATOS
    // ==========================================
    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnGenerarOrden: MaterialButton
    private lateinit var btnVaciar: ImageButton
    private lateinit var llEmptyState: View

    // 🔑 ESTA LISTA SE COMPARTIRÁ DIRECTAMENTE CON EL ADAPTER
    private val listaCarrito = mutableListOf<CarritoItem>()
    private lateinit var adapter: CarritoAdapter

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        // Inicializar vistas
        rvCarrito = findViewById(R.id.rvCarrito)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        btnGenerarOrden = findViewById(R.id.btnGenerarOrden)
        btnVaciar = findViewById(R.id.btnVaciar)
        llEmptyState = findViewById(R.id.llEmptyState)

        setupToolbar()
        setupAdapter()
        setupListeners()

        // Cargar datos iniciales
        sincronizarConCartManager()
    }

    override fun onResume() {
        super.onResume()
        // 🔁 Cada vez que volvemos a esta pantalla, refrescamos desde CartManager
        sincronizarConCartManager()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Carrito"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupAdapter() {
        rvCarrito.layoutManager = LinearLayoutManager(this)
        adapter = CarritoAdapter(listaCarrito) { index ->
            CartManager.removeAt(index)
            sincronizarConCartManager()
        }
        rvCarrito.adapter = adapter
    }

    private fun setupListeners() {
        btnVaciar.setOnClickListener {
            if (listaCarrito.isEmpty()) return@setOnClickListener
            CartManager.clear()
            sincronizarConCartManager()
            Toast.makeText(this, "🗑️ Carrito vaciado", Toast.LENGTH_SHORT).show()
        }

        btnGenerarOrden.setOnClickListener {
            if (listaCarrito.isEmpty()) {
                Toast.makeText(this, "❌ El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnGenerarOrden.isEnabled = false
            btnGenerarOrden.text = "PROCESANDO..."
            generarOrdenServicio()
        }
    }

    // 🔑 MÉTODO CLAVE: Mantiene sincronizada la lista con CartManager SIN cambiar la referencia
    private fun sincronizarConCartManager() {
        listaCarrito.clear()
        listaCarrito.addAll(CartManager.items)
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

    private fun generarOrdenServicio() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    Toast.makeText(this@CarritoActivity, "⚠️ API no disponible", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val userId = prefs.getInt("user_id", 0)
                if (userId == 0) {
                    Toast.makeText(this@CarritoActivity, "❌ Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val orden = Orden_servicio(
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

                val response: Response<ResponseBody> = ApiAndroid.apiService.crearOrdenServicio(orden)

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonString)
                    val success = jsonObject.optBoolean("success", false)

                    if (success) {
                        Toast.makeText(this@CarritoActivity, "✅ Orden generada correctamente", Toast.LENGTH_LONG).show()
                        CartManager.clear()
                        sincronizarConCartManager()
                        finish()
                    } else {
                        val msg = jsonObject.optString("message", "Error desconocido")
                        Toast.makeText(this@CarritoActivity, "❌ $msg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error de conexión"
                    Toast.makeText(this@CarritoActivity, "❌ $errorBody", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CarritoActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnGenerarOrden.isEnabled = true
                btnGenerarOrden.text = "GENERAR ORDEN DE SERVICIO"
            }
        }
    }
}