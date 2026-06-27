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
    private var listaCompleta = mutableListOf<Orden_servicio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_servicio)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()
        setupFab()

        // Cargar al inicio
        cargarOrdenes()
    }

    // Recargar automáticamente al volver de otras pantallas (ej. Carrito, Formulario)
    override fun onResume() {
        super.onResume()
        cargarOrdenes()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

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
        adapter = OrdenServicioAdapter { orden ->
            // 🟢 NAVEGACIÓN A DETALLES DE LA ORDEN
            val intent = Intent(this, OrdenDetalleActivity::class.java).apply {
                putExtra("orden_extra", orden)
            }
            startActivity(intent)
        }
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
        findViewById<FloatingActionButton>(R.id.fabAgregarOrden)?.setOnClickListener {
            // 🟢 NAVEGACIÓN AL FORMULARIO DE NUEVA ORDEN
            startActivity(Intent(this, FormOrdenServicioActivity::class.java))
        }
    }

    private fun cargarOrdenes() {
        lifecycleScope.launch {
            try {
                if (!ApiAndroid.isReady) {
                    Toast.makeText(this@OrdenServicioActivity, "API no disponible", Toast.LENGTH_LONG).show()
                    mostrarVacio()
                    return@launch
                }

                val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
                val token = prefs.getString("auth_token", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(this@OrdenServicioActivity, "No hay sesión activa. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                    mostrarVacio()
                    return@launch
                }

                val retrofitResponse = ApiAndroid.apiService.getMisOrdenes(token = "Bearer $token")
                val resp = retrofitResponse.body()

                if (retrofitResponse.isSuccessful && resp != null) {
                    if (resp.success && resp.data != null) {
                        listaCompleta.clear()
                        listaCompleta.addAll(resp.data)
                        adapter.submitList(resp.data)

                        if (resp.data.isEmpty()) {
                            mostrarVacio()
                            Toast.makeText(this@OrdenServicioActivity, "No tienes ordenes aun", Toast.LENGTH_SHORT).show()
                        } else {
                            llEmptyState.visibility = View.GONE
                            rvOrdenes.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@OrdenServicioActivity, resp.message ?: "Error al obtener ordenes", Toast.LENGTH_SHORT).show()
                        mostrarVacio()
                    }
                } else {
                    Toast.makeText(this@OrdenServicioActivity, "Error de red: ${retrofitResponse.code()}", Toast.LENGTH_LONG).show()
                    mostrarVacio()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrdenServicioActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                mostrarVacio()
            }
        }
    }

    private fun mostrarVacio() {
        llEmptyState.visibility = View.VISIBLE
        rvOrdenes.visibility = View.GONE
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter { orden ->
            orden.idOrden_servicio?.toString()?.contains(query, ignoreCase = true) == true ||
                    orden.estado?.contains(query, ignoreCase = true) == true ||
                    orden.idMotos?.toString()?.contains(query, ignoreCase = true) == true
        }
        adapter.submitList(filtrada)
    }
}