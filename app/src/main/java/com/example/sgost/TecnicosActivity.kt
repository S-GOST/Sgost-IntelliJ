package com.example.sgost

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.TecnicoAdapter
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Tecnico
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class TecnicosActivity : AppCompatActivity() {

    private lateinit var etSearchTecnicos: TextInputEditText
    private lateinit var rvTecnicos: RecyclerView
    private lateinit var fabAgregarTecnico: FloatingActionButton
    private lateinit var llEmptyState: LinearLayout // ✅ Nuevo ID del XML

    private lateinit var adapter: TecnicoAdapter
    // ✅ Lista que guardará todos los datos para poder filtrar sin perder info
    private var listaCompleta = mutableListOf<Tecnico>()

    private val registrarResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        cargarTecnicos() // 🔄 Refresca al volver del form
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnicos)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()
        setupFab()

        cargarTecnicos() // Iniciar carga de datos
    }
    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // NO agregues setNavigationOnClickListener aquí
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initViews() {
        etSearchTecnicos = findViewById(R.id.etSearchTecnicos)
        rvTecnicos = findViewById(R.id.rvTecnicos)
        fabAgregarTecnico = findViewById(R.id.fabAgregarTecnico)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupAdapter() {
        adapter = TecnicoAdapter(
            onEdit = { tecnico -> abrirFormulario(tecnico) },
            onDelete = { tecnico -> confirmarEliminar(tecnico) }
        )
        rvTecnicos.layoutManager = LinearLayoutManager(this)
        rvTecnicos.adapter = adapter
    }

    private fun setupSearch() {
        etSearchTecnicos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
        })
    }

    private fun setupFab() {
        fabAgregarTecnico.setOnClickListener {
            abrirFormulario(null) // 🆕 Crear nuevo
        }
    }

    private fun cargarTecnicos() {
        lifecycleScope.launch {
            try {
                // ✅ Verificar si ApiClient está listo
                if (!ApiClient.isReady) {
                    Toast.makeText(this@TecnicosActivity, "⚠️ Conexión API no lista", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 🔍 Llamada a tu API
                val resp = ApiClient.apiService.obtenerTecnicos()

                if (resp.success && resp.data != null) {
                    // ✅ FIX CRÍTICO: Guardar datos en la lista completa antes de filtrar
                    listaCompleta.clear()
                    listaCompleta.addAll(resp.data)

                    // Mostrar lista en el Adapter
                    adapter.submitList(resp.data)

                    // ✅ Mostrar/Ocultar Estado Vacío
                    if (resp.data.isEmpty()) {
                        llEmptyState.visibility = LinearLayout.VISIBLE
                        rvTecnicos.visibility = RecyclerView.GONE
                    } else {
                        llEmptyState.visibility = LinearLayout.GONE
                        rvTecnicos.visibility = RecyclerView.VISIBLE
                    }

                } else {
                    Toast.makeText(this@TecnicosActivity, resp.message ?: "No hay técnicos", Toast.LENGTH_SHORT).show()
                    // Si falla o viene vacío, mostrar estado vacío
                    llEmptyState.visibility = LinearLayout.VISIBLE
                    rvTecnicos.visibility = RecyclerView.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Lógica corregida del Buscador
    private fun filtrarLista(query: String) {
        // Filtramos sobre la listaCompleta (que tiene todos los datos)
        val filtrada = listaCompleta.filter { tecnico ->
            // Buscamos en nombre o en el campo documento/usuario
            tecnico.nombre?.contains(query, ignoreCase = true) == true ||
                    tecnico.tipoDocumento?.contains(query, ignoreCase = true) == true ||
                    tecnico.usuario?.contains(query, ignoreCase = true) == true
        }
        adapter.submitList(filtrada)
    }

    private fun abrirFormulario(tecnico: Tecnico?) {
        val intent = Intent(this, FormTecnicoActivity::class.java).apply {
            putExtra("tecnico_extra", tecnico)
        }
        registrarResultado.launch(intent)
    }

    private fun confirmarEliminar(tecnico: Tecnico) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar técnico?")
            .setMessage("Se eliminará permanentemente a ${tecnico.nombre}.")
            .setPositiveButton("Sí") { _, _ -> eliminarTecnico(tecnico) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTecnico(tecnico: Tecnico) {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.eliminarTecnico(tecnico.idTecnicos.toString())
                if (resp.success) {
                    Toast.makeText(this@TecnicosActivity, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarTecnicos() // Recargar lista y estado vacío
                } else {
                    Toast.makeText(this@TecnicosActivity, resp.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}