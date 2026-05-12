package com.example.sgost

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.TecnicoAdapter
import com.example.sgost.api.ApiClient
import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Tecnico
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class TecnicosActivity : AppCompatActivity() {

    private lateinit var rvTecnicos: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var adapter: TecnicoAdapter
    private lateinit var llEmptyState: View

    private var allTecnicos = listOf<Tecnico>()
    private var filteredTecnicos = listOf<Tecnico>()

    private val prefs by lazy { getSharedPreferences("sgost_prefs", Context.MODE_PRIVATE) }
    private val token by lazy { prefs.getString("auth_token", "") ?: "" }
    private val authHeader by lazy { if (token.startsWith("Bearer")) token else "Bearer $token" }

    private val formLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) cargarTecnicos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnicos)

        rvTecnicos = findViewById(R.id.rvTecnicos)
        fabAgregar = findViewById(R.id.fabAgregarTecnico)
        etSearch = findViewById(R.id.etSearchTecnicos)
        llEmptyState = findViewById(R.id.llEmptyState)

        setupAdapter()
        setupSearch()
        setupFAB()
        cargarTecnicos()
    }

    private fun setupAdapter() {
        adapter = TecnicoAdapter(
            onEdit = { abrirFormulario(it) },
            onDelete = { confirmarEliminar(it) }
        )
        rvTecnicos.layoutManager = LinearLayoutManager(this)
        rvTecnicos.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) = filtrarTecnicos(s.toString().trim())
        })
    }

    private fun setupFAB() {
        fabAgregar.setOnClickListener { abrirFormulario(null) }
    }

    private fun abrirFormulario(tecnico: Tecnico?) {
        val intent = Intent(this, FormTecnicoActivity::class.java).apply {
            putExtra("IS_EDIT", tecnico != null)
            tecnico?.let { putExtra("TECNICO_DATA", it) }
        }
        formLauncher.launch(intent)
    }

    // ✅ Versión con corrutinas (suspend)
    private fun cargarTecnicos() {
        lifecycleScope.launch {
            try {
                val tecnicos = ApiClient.apiService.obtenerTecnicos(authHeader)
                allTecnicos = tecnicos
                filteredTecnicos = allTecnicos
                adapter.submitList(filteredTecnicos)
                actualizarEstadoVacio()
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filtrarTecnicos(query: String) {
        filteredTecnicos = if (query.isEmpty()) {
            allTecnicos
        } else {
            allTecnicos.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.correo.contains(query, ignoreCase = true) ||
                        it.usuario.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredTecnicos)
        actualizarEstadoVacio()
    }

    private fun actualizarEstadoVacio() {
        rvTecnicos.visibility = if (filteredTecnicos.isEmpty()) View.GONE else View.VISIBLE
        llEmptyState.visibility = if (filteredTecnicos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmarEliminar(tecnico: Tecnico) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Técnico")
            .setMessage("¿Estás seguro de eliminar a ${tecnico.nombre}?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarTecnico(tecnico) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ✅ Eliminación con corrutinas
    private fun eliminarTecnico(tecnico: Tecnico) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.eliminarTecnico(authHeader, tecnico.id)
                if (response.success) {
                    Toast.makeText(this@TecnicosActivity, "✅ Eliminado", Toast.LENGTH_SHORT).show()
                    cargarTecnicos()
                } else {
                    Toast.makeText(this@TecnicosActivity, "❌ ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}