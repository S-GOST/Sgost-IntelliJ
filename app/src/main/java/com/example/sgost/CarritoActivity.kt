package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.sgost.model.Detalles_orden_servicio
import com.example.sgost.model.Orden_servicio
import com.google.android.material.appbar.MaterialToolbar
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

    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnGenerarOrden: MaterialButton
    private lateinit var btnVaciar: ImageButton
    private lateinit var llEmptyState: View

    private val listaCarrito = mutableListOf<CarritoItem>()
    private lateinit var adapter: CarritoAdapter

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        rvCarrito = findViewById(R.id.rvCarrito)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        btnGenerarOrden = findViewById(R.id.btnGenerarOrden)
        btnVaciar = findViewById(R.id.btnVaciar)
        llEmptyState = findViewById(R.id.llEmptyState)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            startActivity(Intent(this, DashboardActivityClientes::class.java))
            finish()
        }

        setupAdapter()
        setupListeners()
        sincronizarConCartManager()
    }

    override fun onResume() {
        super.onResume()
        sincronizarConCartManager()
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

                // 1. CREAR CABECERA DE LA ORDEN
                val orden = Orden_servicio(
                    idOrden_servicio = null,
                    idClientes = userId,
                    idMotos = null,
                    idAdministrador = 1,
                    idTecnicos = 1,
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = formatoFecha.format(Date(System.currentTimeMillis() + 86400000)),
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                val responseOrden: Response<ResponseBody> = ApiAndroid.apiService.crearOrdenServicio(orden)
                if (!responseOrden.isSuccessful) {
                    Toast.makeText(this@CarritoActivity, "❌ ${responseOrden.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val jsonOrden = JSONObject(responseOrden.body()?.string() ?: "{}")
                if (!jsonOrden.optBoolean("success", false)) {
                    Toast.makeText(this@CarritoActivity, "❌ ${jsonOrden.optString("message", "Error")}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val idOrdenNueva = jsonOrden.optJSONObject("data")?.optInt("ID_ORDEN_SERVICIO", 0) ?: 0
                if (idOrdenNueva == 0) {
                    Toast.makeText(this@CarritoActivity, "❌ No se recibió ID de orden", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 🔑 2. AGREGACIÓN: Todos los ítems del carrito en UNA SOLA FILA
                var servicioId: Int? = null
                var productoId: Int? = null
                val nombresServicios = mutableListOf<String>()
                val nombresProductos = mutableListOf<String>()
                var precioTotal = 0.0
                var garantiaMax = 0

                for (item in listaCarrito) {
                    precioTotal += item.subtotal
                    if (item.garantia != null && item.garantia > garantiaMax) {
                        garantiaMax = item.garantia!!
                    }

                    if (item.idServicio != null) {
                        if (servicioId == null) servicioId = item.idServicio // Toma el primer servicio
                        val nombreServ = item.nombre ?: "Servicio"
                        if (!nombresServicios.contains(nombreServ)) nombresServicios.add(nombreServ)
                    }

                    if (item.idProducto != null) {
                        if (productoId == null) productoId = item.idProducto // Toma el primer producto
                        val nombreProd = item.nombre ?: "Producto"
                        if (!nombresProductos.contains(nombreProd)) nombresProductos.add(nombreProd)
                    }
                }

                // 3. CREAR ÚNICO DETALLE AGREGADO
                val detalle = Detalles_orden_servicio(
                    idOrden = idOrdenNueva,
                    idServicios = servicioId,
                    idProductos = productoId,
                    nombreServicio = nombresServicios.joinToString(", "),
                    nombreProducto = nombresProductos.joinToString(", "),
                    precio = precioTotal,
                    garantia = garantiaMax
                )

                Log.d("CARRITO_DEBUG", "📦 Enviando 1 detalle agregado -> ServicioID: $servicioId | ProductoID: $productoId | PrecioTotal: $precioTotal")

                // 4. ENVIAR UNA SOLA VEZ AL BACKEND
                val response = ApiAndroid.apiService.crearDetalleOrden(detalle)
                if (response.success) {
                    Toast.makeText(this@CarritoActivity, "✅ Orden creada y guardada en 1 fila", Toast.LENGTH_LONG).show()
                    CartManager.clear()
                    sincronizarConCartManager()
                    finish()
                } else {
                    Toast.makeText(this@CarritoActivity, "⚠️ Falló el detalle: ${response.message}", Toast.LENGTH_LONG).show()
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