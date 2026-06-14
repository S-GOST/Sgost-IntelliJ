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

        // ✅ CONFIGURACIÓN ÚNICA Y CORRECTA DEL TOOLBAR
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

    // ✅ MANEJO CORRECTO DEL BOTÓN DE RETROCESO
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

                if (respDetalles.success && respDetalles.data != null) {
                    val listaRaw = respDetalles.data!!
                    val listaServicios = respServicios.data.orEmpty()
                    val listaProductos = respProductos.data.orEmpty()

                    Log.d("ORDEN_DETALLE", "========== DIAGNÓSTICO ==========")
                    Log.d("ORDEN_DETALLE", "📦 Filas crudas del API: ${listaRaw.size}")
                    listaRaw.forEachIndexed { i, d ->
                        Log.d("ORDEN_DETALLE", "  RAW[$i] idDetalle=${d.idDetalleOrden} idServ=${d.idServicios} idProd=${d.idProductos} nombreServ=${d.nombreServicio} nombreProd=${d.nombreProducto} precio=${d.precio}")
                    }
                    Log.d("ORDEN_DETALLE", "📋 Catálogo servicios: ${listaServicios.size}")
                    listaServicios.forEach { s ->
                        Log.d("ORDEN_DETALLE", "  SERV id=${s.idServicios} nombre=${s.nombre} precio=${s.precio}")
                    }
                    Log.d("ORDEN_DETALLE", "📋 Catálogo productos: ${listaProductos.size}")
                    listaProductos.forEach { p ->
                        Log.d("ORDEN_DETALLE", "  PROD id=${p.idProductos} nombre=${p.nombre} marca=${p.marca} precio=${p.precio}")
                    }
                    Log.d("ORDEN_DETALLE", "==================================")

                    val elementosFinales = mutableListOf<Detalles_orden_servicio>()

                    for (detalle in listaRaw) {
                        val tieneServicio = detalle.idServicios != null && detalle.idServicios > 0
                        val tieneProducto = detalle.idProductos != null && detalle.idProductos > 0

                        Log.d("ORDEN_DETALLE", "🔍 Procesando detalle #${detalle.idDetalleOrden}: tieneServicio=$tieneServicio (id=${detalle.idServicios}), tieneProducto=$tieneProducto (id=${detalle.idProductos})")

                        if (tieneServicio) {
                            val servicio = listaServicios.find { it.idServicios == detalle.idServicios }
                            Log.d("ORDEN_DETALLE", "  🔧 Servicio encontrado en catálogo: ${servicio != null} (buscando id=${detalle.idServicios})")
                            if (servicio != null) {
                                Log.d("ORDEN_DETALLE", "  ✅ Agregando servicio: ${servicio.nombre} precio=${servicio.precio}")
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
                            } else {
                                val nombreFallback = detalle.nombreServicio
                                Log.d("ORDEN_DETALLE", "  ⚠️ Servicio NO encontrado en catálogo, usando fallback: $nombreFallback")
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = detalle.idServicios,
                                        idProductos = null,
                                        nombreServicio = nombreFallback ?: "Servicio #${detalle.idServicios}",
                                        nombreProducto = null,
                                        precio = detalle.precio ?: 0.0,
                                        garantia = detalle.garantia ?: 0
                                    )
                                )
                            }
                        }

                        if (tieneProducto) {
                            val producto = listaProductos.find { it.idProductos == detalle.idProductos }
                            Log.d("ORDEN_DETALLE", "  📦 Producto encontrado en catálogo: ${producto != null} (buscando id=${detalle.idProductos})")
                            if (producto != null) {
                                val nombreProducto = "${producto.marca ?: ""} ${producto.nombre ?: ""}".trim()
                                Log.d("ORDEN_DETALLE", "  ✅ Agregando producto: $nombreProducto precio=${producto.precio}")
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = null,
                                        idProductos = detalle.idProductos,
                                        nombreServicio = null,
                                        nombreProducto = nombreProducto.ifEmpty { producto.nombre },
                                        precio = producto.precio ?: 0.0,
                                        garantia = producto.garantia ?: 0
                                    )
                                )
                            } else {
                                val nombreFallback = detalle.nombreProducto
                                Log.d("ORDEN_DETALLE", "  ⚠️ Producto NO encontrado en catálogo, usando fallback: $nombreFallback")
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = null,
                                        idProductos = detalle.idProductos,
                                        nombreServicio = null,
                                        nombreProducto = nombreFallback ?: "Producto #${detalle.idProductos}",
                                        precio = detalle.precio ?: 0.0,
                                        garantia = detalle.garantia ?: 0
                                    )
                                )
                            }
                        }

                        if (!tieneServicio && !tieneProducto) {
                            val tieneNombreServ = !detalle.nombreServicio.isNullOrEmpty()
                            val tieneNombreProd = !detalle.nombreProducto.isNullOrEmpty()

                            Log.d("ORDEN_DETALLE", "  ❓ Sin IDs. nombreServ=$tieneNombreServ nombreProd=$tieneNombreProd")

                            if (tieneNombreServ && tieneNombreProd) {
                                val servicioCat = listaServicios.find {
                                    it.nombre.equals(detalle.nombreServicio, ignoreCase = true)
                                }
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = servicioCat?.idServicios ?: -1,
                                        idProductos = null,
                                        nombreServicio = detalle.nombreServicio,
                                        nombreProducto = null,
                                        precio = servicioCat?.precio ?: detalle.precio ?: 0.0,
                                        garantia = servicioCat?.garantia ?: detalle.garantia ?: 0
                                    )
                                )
                                Log.d("ORDEN_DETALLE", "  ✅ Separado SERVICIO: ${detalle.nombreServicio} (catálogo: ${servicioCat != null})")

                                val productoCat = listaProductos.find {
                                    it.nombre.equals(detalle.nombreProducto, ignoreCase = true)
                                }
                                elementosFinales.add(
                                    Detalles_orden_servicio(
                                        idDetalleOrden = detalle.idDetalleOrden,
                                        idOrden = detalle.idOrden,
                                        idServicios = null,
                                        idProductos = productoCat?.idProductos ?: -1,
                                        nombreServicio = null,
                                        nombreProducto = detalle.nombreProducto,
                                        precio = productoCat?.precio ?: detalle.precio ?: 0.0,
                                        garantia = productoCat?.garantia ?: detalle.garantia ?: 0
                                    )
                                )
                                Log.d("ORDEN_DETALLE", "  ✅ Separado PRODUCTO: ${detalle.nombreProducto} (catálogo: ${productoCat != null})")

                            } else if (tieneNombreServ) {
                                val servicioCat = listaServicios.find {
                                    it.nombre.equals(detalle.nombreServicio, ignoreCase = true)
                                }
                                elementosFinales.add(
                                    detalle.copy(
                                        idServicios = servicioCat?.idServicios ?: -1,
                                        precio = servicioCat?.precio ?: detalle.precio,
                                        garantia = servicioCat?.garantia ?: detalle.garantia
                                    )
                                )
                            } else if (tieneNombreProd) {
                                val productoCat = listaProductos.find {
                                    it.nombre.equals(detalle.nombreProducto, ignoreCase = true)
                                }
                                elementosFinales.add(
                                    detalle.copy(
                                        idProductos = productoCat?.idProductos ?: -1,
                                        precio = productoCat?.precio ?: detalle.precio,
                                        garantia = productoCat?.garantia ?: detalle.garantia
                                    )
                                )
                            } else {
                                elementosFinales.add(detalle)
                            }
                        }
                    }

                    Log.d("ORDEN_DETALLE", "📊 elementosFinales antes de filtrar: ${elementosFinales.size}")
                    elementosFinales.forEachIndexed { i, it ->
                        Log.d("ORDEN_DETALLE", "  [$i] idServ=${it.idServicios} idProd=${it.idProductos} nombreServ=${it.nombreServicio} nombreProd=${it.nombreProducto} precio=${it.precio}")
                    }

                    val listaFinal = elementosFinales.filter {
                        !it.nombreServicio.isNullOrEmpty() || !it.nombreProducto.isNullOrEmpty()
                    }

                    Log.d("ORDEN_DETALLE", "🎯 Elementos a mostrar: ${listaFinal.size}")
                    listaFinal.forEach {
                        val tipo = if (it.idServicios != null && it.idServicios > 0) "SERVICIO" else "PRODUCTO"
                        Log.d("ORDEN_DETALLE", "   [$tipo] ${it.nombreServicio ?: it.nombreProducto} | Precio: ${it.precio} | Garantía: ${it.garantia}")
                    }

                    withContext(Dispatchers.Main) {
                        adapter.submitList(listaFinal)
                        if (listaFinal.isNotEmpty()) {
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