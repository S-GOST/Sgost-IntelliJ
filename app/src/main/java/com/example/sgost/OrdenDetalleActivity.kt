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
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

                val detallesJob = async { ApiAndroid.apiService.obtenerDetallesPorOrden(idOrden.toString()) }
                val serviciosJob = async { ApiAndroid.apiService.obtenerServicios() }
                val productosJob = async { ApiAndroid.apiService.obtenerProductos() }

                val respDetalles = detallesJob.await()
                val respServicios = serviciosJob.await()
                val respProductos = productosJob.await()

                // ---------------------------------------------------------
                // ✅ CORRECCIÓN DE ACCESO A DATOS (INCONSISTENCIA DE API)
                // ---------------------------------------------------------

                // 1. Detalles: Tu código anterior funcionaba con .data directo
                val listaRaw = respDetalles.data ?: emptyList()

                // 2. Servicios: Tu ServicioRepository usa Response<ApiResponse>,
                // por lo que aquí DEBES usar .body()?.data para evitar el crash de 403
                val listaServicios = respServicios.body()?.data ?: emptyList()

                // 3. Productos: Tu ProductoRepository usa ApiResponse directo
                val listaProductos = respProductos.body()?.data ?: emptyList()

                val apiSuccess = respDetalles.success == true

                if (apiSuccess && listaRaw.isNotEmpty()) {
                    Log.d("ORDEN_DETALLE", "========== DIAGNÓSTICO ==========")
                    Log.d("ORDEN_DETALLE", "📦 Filas crudas del API: ${listaRaw.size}")
                    listaRaw.forEachIndexed { i, d ->
                        Log.d("ORDEN_DETALLE", "  RAW[$i] idDetalle=${d.idDetalleOrden} idServ=${d.idServicios} idProd=${d.idProductos}")
                    }

                    Log.d("ORDEN_DETALLE", "📋 Catálogo servicios: ${listaServicios.size}")
                    Log.d("ORDEN_DETALLE", "📋 Catálogo productos: ${listaProductos.size}")
                    Log.d("ORDEN_DETALLE", "==================================")

                    val elementosFinales = mutableListOf<Detalles_orden_servicio>()

                    for (detalle in listaRaw) {
                        val tieneServicio = detalle.idServicios != null && detalle.idServicios > 0
                        val tieneProducto = detalle.idProductos != null && detalle.idProductos > 0

                        if (tieneServicio) {
                            val servicio = listaServicios.find { it.idServicios == detalle.idServicios }
                            if (servicio != null) {
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = detalle.idServicios,
                                        idProductos = null,
                                        nombreServicio = servicio.nombre,
                                        nombreProducto = null,
                                        precio = servicio.precio ?: 0.0,
                                        garantia = servicio.garantia ?: 0
                                    )
                                )
                            }
                        }

                        if (tieneProducto) {
                            val producto = listaProductos.find { it.idProductos == detalle.idProductos }
                            if (producto != null) {
                                val nombreCompleto = "${producto.marca ?: ""} ${producto.nombre}".trim()
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = null,
                                        idProductos = detalle.idProductos,
                                        nombreServicio = null,
                                        nombreProducto = nombreCompleto,
                                        precio = producto.precio ?: 0.0,
                                        garantia = producto.garantia ?: 0
                                    )
                                )
                            }
                        }

                        // Fallback si no hay IDs pero hay nombres
                        if (!tieneServicio && !tieneProducto && (!detalle.nombreServicio.isNullOrEmpty() || !detalle.nombreProducto.isNullOrEmpty())) {
                            elementosFinales.add(detalle)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        adapter.submitList(elementosFinales)
                        if (elementosFinales.isNotEmpty()) {
                            rvDetalles.visibility = View.VISIBLE
                            llEmptyState.visibility = View.GONE
                        } else {
                            showEmptyState()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("⚠️ ${respDetalles.message ?: "Sin detalles"}")
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ORDEN_DETALLE", "Error: ${e.message}", e)
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