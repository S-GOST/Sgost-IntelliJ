package com.example.sgost

import android.os.Bundle
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
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Dispatchers
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

        // Obtener ID de la orden desde el Intent
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

                withContext(Dispatchers.Main) {
                    if (resp.success && resp.data != null) {
                        Log.d("DETALLES_API", "✅ Success: true | Size: ${resp.data.size}")
                        Log.d("DETALLES_API", "📦 Data: ${resp.data}")

                        adapter.submitList(resp.data)
                        // Forzar refresh por si hay cache del adapter
                        adapter.notifyDataSetChanged()

                        if (resp.data.isEmpty()) {
                            llEmptyState.visibility = View.VISIBLE
                            rvDetalles.visibility = View.GONE
                        } else {
                            llEmptyState.visibility = View.GONE
                            rvDetalles.visibility = View.VISIBLE
                        }
                    } else {
                        Log.e("DETALLES_API", "❌ Error: ${resp.message}")
                        showToast("❌ ${resp.message ?: "Error"}")
                        llEmptyState.visibility = View.VISIBLE
                        rvDetalles.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DETALLES_API", "💥 Excepción: ${e.message}", e)
                    showToast("❌ ${e.message}")
                    llEmptyState.visibility = View.VISIBLE
                    rvDetalles.visibility = View.GONE
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}