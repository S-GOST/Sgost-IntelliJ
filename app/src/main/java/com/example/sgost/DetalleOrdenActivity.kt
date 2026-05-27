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
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    showToast("⚠️ Conexión API no disponible")
                    return@launch
                }

                // 🔽 Agrega este método en tu ApiService:
                val resp = ApiAndroid.apiService.obtenerDetallesPorOrden(idOrden.toString())

                if (resp.success && resp.data != null) {
                    adapter.submitList(resp.data)

                    if (resp.data.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                        rvDetalles.visibility = View.GONE
                    } else {
                        llEmptyState.visibility = View.GONE
                        rvDetalles.visibility = View.VISIBLE
                    }
                } else {
                    showToast("❌ ${resp.message ?: "No se pudieron cargar los detalles"}")
                }
            } catch (e: Exception) {
                showToast("❌ Error: ${e.message}")
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}