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
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Administrador
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AdministradoresActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvList: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var llEmptyState: LinearLayout

    private lateinit var adapter: AdministradorAdapter
    private var listaCompleta = mutableListOf<Administrador>()

    private val registrarResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        cargarAdministradores()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administradores)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()
        setupFab()

        cargarAdministradores()
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
        rvList = findViewById(R.id.rvList)
        fabAdd = findViewById(R.id.fabAdd)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    // ✅ CORREGIDO: Se agregaron ambos parámetros (onEdit y onDelete) y se cerró correctamente
    private fun setupAdapter() {
        adapter = AdministradorAdapter(
            onEdit = { admin ->
                startActivity(Intent(this, FormAdministradorActivity::class.java).apply {
                    putExtra("admin_extra", admin)
                })
            },
            onDelete = { admin ->
                confirmarEliminar(admin)
            }
        )
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = adapter
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

    // ✅ ACTIVO: Ahora el botón flotante abre el formulario para crear
    private fun setupFab() {
        fabAdd.setOnClickListener {
            startActivity(Intent(this, FormAdministradorActivity::class.java))
        }
    }

    private fun cargarAdministradores() {
        lifecycleScope.launch {
            try {
                if (!ApiClient.isReady) {
                    Toast.makeText(this@AdministradoresActivity, "⚠️ API no lista", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val resp = ApiClient.apiService.obtenerAdministradores()

                if (resp.success && resp.data != null) {
                    listaCompleta.clear()
                    listaCompleta.addAll(resp.data)
                    adapter.submitList(resp.data)

                    if (resp.data.isEmpty()) {
                        llEmptyState.visibility = LinearLayout.VISIBLE
                        rvList.visibility = RecyclerView.GONE
                    } else {
                        llEmptyState.visibility = LinearLayout.GONE
                        rvList.visibility = RecyclerView.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AdministradoresActivity, resp.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdministradoresActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter { admin ->
            admin.nombre?.contains(query, ignoreCase = true) == true ||
                    admin.usuario?.contains(query, ignoreCase = true) == true
        }
        adapter.submitList(filtrada)
    }

    private fun confirmarEliminar(admin: Administrador) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar administrador?")
            .setMessage("Se eliminará permanentemente a ${admin.nombre}.")
            .setPositiveButton("Sí") { _, _ -> eliminarAdministrador(admin) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarAdministrador(admin: Administrador) {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.eliminarAdministradores(admin.id.toString())

                if (resp.success) {
                    Toast.makeText(this@AdministradoresActivity, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarAdministradores()
                } else {
                    Toast.makeText(this@AdministradoresActivity, resp.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdministradoresActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}