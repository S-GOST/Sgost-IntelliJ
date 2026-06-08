package com.example.sgost

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdenDetalleActivity : AppCompatActivity() {

    private lateinit var tvOrdenInfo: TextView
    private lateinit var rvDetalles: RecyclerView
    private lateinit var llEmptyState: LinearLayout

    private lateinit var adapter: DetalleOrdenAdapter
    private var idOrden: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_detalle)

        setupToolbar()
        initViews()
        setupAdapter()

        val orden = intent.getParcelableExtra<Orden_servicio>("orden_extra")
        idOrden = orden?.idOrden_servicio

        if (idOrden != null) {
            tvOrdenInfo.text = "Orden #${idOrden} • ${orden?.estado ?: ""}"
            cargarDetalles()
        } else {
            Toast.makeText(this, "❌ Error: No se recibió la orden", Toast.LENGTH_SHORT).show()
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
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        tvOrdenInfo = findViewById(R.id.tvOrdenInfo)
        rvDetalles = findViewById(R.id.rvDetalles)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupAdapter() {
        adapter = DetalleOrdenAdapter()
        rvDetalles.layoutManager = LinearLayoutManager(this)
        rvDetalles.adapter = adapter
    }

    private fun cargarDetalles() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!ApiAndroid.isReady) {
                    withContext(Dispatchers.Main) { showToast("⚠️ API no disponible") }
                    return@launch
                }

                val resp = ApiAndroid.apiService.obtenerDetallesPorOrden(idOrden.toString())
                val listaRaw = resp.data?.filterNotNull() ?: emptyList()
                Log.d("DETALLES_API", "📥 Registros crudos: ${listaRaw.size}")

                withContext(Dispatchers.Main) {
                    // ✅ SEPARA SERVICIO Y PRODUCTO SI VIENEN EN LA MISMA FILA
                    val listaFinal = listaRaw.flatMap { detalle ->
                        val items = mutableListOf<Detalles_orden_servicio>()
                        val tieneServicio = !detalle.nombreServicio.isNullOrEmpty() || (detalle.idServicios != null && detalle.idServicios!! > 0)
                        val tieneProducto = !detalle.nombreProducto.isNullOrEmpty() || (detalle.idProductos != null && detalle.idProductos!! > 0)

                        if (tieneServicio && tieneProducto) {
                            items.add(detalle.copy(nombreProducto = null, idProductos = null))
                            items.add(detalle.copy(nombreServicio = null, idServicios = null))
                        } else {
                            items.add(detalle)
                        }
                        items
                    }

                    Log.d("DETALLES_API", "📤 Items a renderizar: ${listaFinal.size}")

                    if (listaFinal.isNotEmpty()) {
                        adapter.submitList(listaFinal)
                        rvDetalles.visibility = View.VISIBLE
                        llEmptyState.visibility = View.GONE
                    } else {
                        adapter.submitList(emptyList())
                        rvDetalles.visibility = View.GONE
                        llEmptyState.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DETALLES_API", "💥 Error: ${e.message}", e)
                    showToast("❌ ${e.message}")
                    rvDetalles.visibility = View.GONE
                    llEmptyState.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}