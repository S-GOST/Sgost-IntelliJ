package com.example.sgost

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.OrdenServicioAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Orden_servicio
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class OrdenServicioActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvOrdenes: RecyclerView
    private lateinit var llEmptyState: LinearLayout

    // ✅ CORREGIDO: Nombre exacto del Adapter que creaste
    private lateinit var adapter: OrdenServicioAdapter
    private var listaCompleta = mutableListOf<Orden_servicio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_servicio)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()

        cargarOrdenes()
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
        etSearch = findViewById(R.id.etSearch)
        rvOrdenes = findViewById(R.id.rvOrdenes)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupAdapter() {
        adapter = OrdenServicioAdapter(onOrderClick = { orden ->
            // Aquí navegas al detalle de la orden
            Toast.makeText(this, "Abriendo detalles de Orden #${orden.idOrden_servicio}", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, OrdenDetalleActivity::class.java).apply {
            //     putExtra("orden_extra", orden)
            // })
        })
        rvOrdenes.layoutManager = LinearLayoutManager(this)
        rvOrdenes.adapter = adapter
    }
    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
        })
    }

    private fun cargarOrdenes() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    showToast("⚠️ Conexión API no disponible")
                    return@launch
                }

                val response = ApiAndroid.apiService.obtenerOrdenServicio()

                if (response.success && response.data != null) {
                    listaCompleta.clear()
                    listaCompleta.addAll(response.data) // ✅ Sin !! innecesario
                    adapter.submitList(response.data)

                    if (response.data.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                        rvOrdenes.visibility = View.GONE
                    } else {
                        llEmptyState.visibility = View.GONE
                        rvOrdenes.visibility = View.VISIBLE
                    }
                } else {
                    showToast("❌ ${response.message ?: "No se pudieron cargar las órdenes"}")
                }
            } catch (e: Exception) {
                showToast("❌ Error: ${e.message}")
            }
        }
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter { orden ->
            val idMatch = orden.idOrden_servicio?.toString()?.contains(query, ignoreCase = true) == true
            val estadoMatch = orden.estado?.contains(query, ignoreCase = true) == true
            idMatch || estadoMatch
        }
        adapter.submitList(filtrada)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}