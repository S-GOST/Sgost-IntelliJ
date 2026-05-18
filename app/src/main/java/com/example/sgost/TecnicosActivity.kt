package com.example.sgost

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.TecnicoAdapter
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Tecnico
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TecnicosActivity : AppCompatActivity() {

    private lateinit var rvTecnicos: RecyclerView
    private lateinit var adapter: TecnicoAdapter
    private lateinit var fabAdd: FloatingActionButton
    private var listaCompleta = mutableListOf<Tecnico>()

    private val registrarResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        cargarTecnicos() // 🔄 Refresca al volver del form
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnicos)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(getDrawable(android.R.drawable.ic_menu_revert))
        }
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvTecnicos = findViewById(R.id.rvTecnicos)
        rvTecnicos.layoutManager = LinearLayoutManager(this)

        // 🔗 Configurar Adapter
        adapter = TecnicoAdapter(
            onEdit = { tecnico -> abrirFormulario(tecnico) },
            onDelete = { tecnico -> confirmarEliminar(tecnico) }
        )
        rvTecnicos.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAgregarTecnico).setOnClickListener {
            abrirFormulario(null) // 🆕 Crear nuevo
        }

        // 🔍 Buscador en tiempo real
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchTecnicos)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filtrarLista(s.toString())
                }
            })

        cargarTecnicos()
    }

    private fun cargarTecnicos() {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.obtenerTecnicos()
                if (resp.success && resp.data != null) {
                    adapter.submitList(resp.data)
                } else {
                    Toast.makeText(this@TecnicosActivity, resp.message ?: "No hay técnicos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter {
            it.nombre?.contains(query, ignoreCase = true) == true ||
                    it.usuario?.contains(query, ignoreCase = true) == true
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
                    cargarTecnicos()
                } else {
                    Toast.makeText(this@TecnicosActivity, resp.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TecnicosActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}