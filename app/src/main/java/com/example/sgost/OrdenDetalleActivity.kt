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
            Toast.makeText(this, "❌ Error: Datos de la orden inválidos", Toast.LENGTH_SHORT).show()
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
    }

    private fun setupAdapter() {
        adapter = DetalleOrdenAdapter()
        rvDetalles.layoutManager = LinearLayoutManager(this)
        rvDetalles.adapter = adapter
    }

    private fun cargarDetalles(idOrden: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!ApiAndroid.isReady) {
                    withContext(Dispatchers.Main) { showToast("⚠️ API no disponible") }
                    return@launch
                }

                val resp = ApiAndroid.apiService.obtenerDetallesPorOrden(idOrden.toString())

                if (resp.success && resp.data != null) {
                    val listaRaw = resp.data!!
                    Log.d("ORDEN_DETALLE", "📥 Registros crudos: ${listaRaw.size}")

                    withContext(Dispatchers.Main) {
                        // Separamos servicio y producto si vienen en la misma fila
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

                        adapter.submitList(listaFinal)
                        rvDetalles.visibility = if (listaFinal.isNotEmpty()) View.VISIBLE else View.GONE
                        llEmptyState.visibility = if (listaFinal.isNotEmpty()) View.GONE else View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("⚠️ ${resp.message ?: "No hay detalles"}")
                        rvDetalles.visibility = View.GONE
                        llEmptyState.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ORDEN_DETALLE", "💥 Error: ${e.message}", e)
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