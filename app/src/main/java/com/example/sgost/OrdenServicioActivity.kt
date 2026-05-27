package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class OrdenServicioActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvOrdenes: RecyclerView
    private lateinit var llEmptyState: LinearLayout

    private lateinit var adapter: OrdenServicioAdapter
    // ✅ Lista que guardará todos los datos para poder filtrar sin perder info
    private var listaCompleta = mutableListOf<Orden_servicio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_servicio)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()
        setupFab() // ✅ Agregado para activar el botón +
        cargarOrdenes() // Iniciar carga de datos
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // NO agregues setNavigationOnClickListener aquí
    }

    // ✅ Manejo oficial del botón de retroceso
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        rvOrdenes = findViewById(R.id.rvOrdenes)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupAdapter() {
        adapter = OrdenServicioAdapter(onOrderClick = { orden ->
            // ✅ Navega a la pantalla de detalles de ESTA orden específica
            startActivity(Intent(this, OrdenDetalleActivity::class.java).apply {
                putExtra("orden_extra", orden)
            })
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

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAgregarOrden).setOnClickListener {
            abrirFormulario(null) // ✅ Crea nueva orden
        }
    }

    // ✅ Método centralizado para abrir el formulario de cabecera (Crear o Editar)
    private fun abrirFormulario(orden: Orden_servicio?) {
        startActivity(Intent(this, FormOrdenServicioActivity::class.java).apply {
            orden?.let { putExtra("orden_extra", it) }
        })
    }

    private fun cargarOrdenes() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    Toast.makeText(this@OrdenServicioActivity, "⚠️ Conexión API no lista", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val resp = ApiAndroid.apiService.obtenerOrdenServicio()

                if (resp.success && resp.data != null) {
                    listaCompleta.clear()
                    listaCompleta.addAll(resp.data)
                    adapter.submitList(resp.data)

                    if (resp.data.isEmpty()) {
                        llEmptyState.visibility = LinearLayout.VISIBLE
                        rvOrdenes.visibility = RecyclerView.GONE
                    } else {
                        llEmptyState.visibility = LinearLayout.GONE
                        rvOrdenes.visibility = RecyclerView.VISIBLE
                    }
                } else {
                    Toast.makeText(this@OrdenServicioActivity, resp.message ?: "No hay órdenes", Toast.LENGTH_SHORT).show()
                    llEmptyState.visibility = LinearLayout.VISIBLE
                    rvOrdenes.visibility = RecyclerView.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrdenServicioActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter { orden ->
            orden.idOrden_servicio?.toString()?.contains(query, ignoreCase = true) == true ||
                    orden.estado?.contains(query, ignoreCase = true) == true
        }
        adapter.submitList(filtrada)
    }
}