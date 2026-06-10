package com.example.sgost

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.DetalleOrdenAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Detalles_orden_servicio
import com.example.sgost.model.Orden_servicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async // ✅ Agregado para peticiones en paralelo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdenDetalleActivity : AppCompatActivity() {

    private lateinit var tvOrdenInfo: TextView
    private lateinit var rvDetalles: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: DetalleOrdenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_detalle)

        setupToolbar()
        initViews()
        setupAdapter()

        val orden = intent.getParcelableExtra<Orden_servicio>("orden_extra")
        val idOrden = orden?.idOrden_servicio

        if (idOrden != null && idOrden > 0) {
            tvOrdenInfo.text = "Orden #${idOrden} • ${orden?.estado ?: "Pendiente"}"
            cargarDetalles(idOrden)
        } else {
            showToast("❌ Error: Datos de la orden inválidos")
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        tvOrdenInfo = findViewById(R.id.tvOrdenInfo)
        rvDetalles = findViewById(R.id.rvDetalles)
        llEmptyState = findViewById(R.id.llEmptyState)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupAdapter() {
        adapter = DetalleOrdenAdapter()
        rvDetalles.layoutManager = LinearLayoutManager(this)
        rvDetalles.adapter = adapter
    }

    private fun cargarDetalles(idOrden: Int) {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    withContext(Dispatchers.Main) {
                        showToast("⚠️ API no disponible")
                        showEmptyState()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) { progressBar.visibility = View.VISIBLE }

                // ✅ 1. Descargamos Detalles, Servicios y Productos AL MISMO TIEMPO
                val detallesJob = async { ApiAndroid.apiService.obtenerDetallesPorOrden(idOrden.toString()) }
                val serviciosJob = async { ApiAndroid.apiService.obtenerServicios() }
                val productosJob = async { ApiAndroid.apiService.obtenerProductos() }

                val resp = detallesJob.await()
                val respServicios = serviciosJob.await()
                val respProductos = productosJob.await()

                if (resp.success && resp.data != null) {
                    val listaRaw = resp.data!!
                    val listaServicios = respServicios.data.orEmpty()
                    val listaProductos = respProductos.data.orEmpty()

                    Log.d("ORDEN_DETALLE", "📥 Registros crudos: ${listaRaw.size}")

                    withContext(Dispatchers.Main) {
                        // ✅ 2. Separar servicio y producto si vienen mezclados en la misma fila
                        val listaSeparada = listaRaw.flatMap { detalle ->
                            val tieneServicio = detalle.idServicios != null && detalle.idServicios > 0
                            val tieneProducto = detalle.idProductos != null && detalle.idProductos > 0

                            if (tieneServicio && tieneProducto) {
                                mutableListOf(
                                    detalle.copy(nombreProducto = null, idProductos = null),
                                    detalle.copy(nombreServicio = null, idServicios = null)
                                )
                            } else {
                                mutableListOf(detalle)
                            }
                        }

                        // ✅ 3. Cruzar los IDs con los catálogos para inyectar precios y nombres reales
                        val listaMapeada = listaSeparada.map { detalle ->
                            var nombreReal: String? = null
                            var precioReal: Double? = detalle.precio
                            var garantiaReal: Int? = detalle.garantia

                            // Buscar datos del servicio
                            if (detalle.idServicios != null && detalle.idServicios > 0) {
                                val servicio = listaServicios.find { it.idServicios == detalle.idServicios }
                                if (servicio != null) {
                                    nombreReal = servicio.nombre
                                    precioReal = servicio.precio ?: 0.0
                                    garantiaReal = servicio.garantia?.toString()?.toIntOrNull() ?: 0
                                }
                            }

                            // Buscar datos del producto
                            if (detalle.idProductos != null && detalle.idProductos > 0) {
                                val producto = listaProductos.find { it.idProductos == detalle.idProductos }
                                if (producto != null) {
                                    nombreReal = "${producto.marca ?: ""} ${producto.nombre ?: ""}".trim()
                                    precioReal = producto.precio ?: 0.0
                                    garantiaReal = producto.garantia?.toString()?.toIntOrNull() ?: 0
                                }
                            }

                            // Retornar detalle con los valores actualizados
                            detalle.copy(
                                nombreServicio = if (detalle.idServicios != null && detalle.idServicios > 0) nombreReal else detalle.nombreServicio,
                                nombreProducto = if (detalle.idProductos != null && detalle.idProductos > 0) nombreReal else detalle.nombreProducto,
                                precio = precioReal,
                                garantia = garantiaReal
                            )
                        }

                        // ✅ 4. Filtrar validando nulos de manera segura
                        val listaFinal = listaMapeada.filter {
                            (!it.nombreServicio.isNullOrEmpty() || !it.nombreProducto.isNullOrEmpty()) && (it.precio ?: 0.0) >= 0.0
                            // Nota: Cambié a >= 0.0 por si tienes servicios gratuitos (precio 0),
                            // pero si exiges que cuesten algo, puedes ponerlo en > 0.0
                        }

                        adapter.submitList(listaFinal)

                        // ✅ 5. Actualizar UI según resultado
                        if (listaFinal.isNotEmpty()) {
                            rvDetalles.visibility = View.VISIBLE
                            llEmptyState.visibility = View.GONE
                        } else {
                            showEmptyState()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("⚠️ ${resp.message ?: "Sin detalles en esta orden"}")
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ORDEN_DETALLE", "💥 Error: ${e.message}", e)
                    showToast("❌ ${e.message}")
                    showEmptyState()
                }
            } finally {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
            }
        }
    }

    private fun showEmptyState() {
        rvDetalles.visibility = View.GONE
        llEmptyState.visibility = View.VISIBLE
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}