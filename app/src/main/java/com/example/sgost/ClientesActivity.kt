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
import com.example.sgost.ClienteAdapter // ✅ Ahora sí será reconocido
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Cliente
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ClientesActivity : AppCompatActivity() {

    private lateinit var etSearchClientes: TextInputEditText
    private lateinit var rvClientes: RecyclerView
    private lateinit var fabAgregarCliente: FloatingActionButton
    private lateinit var llEmptyState: LinearLayout

    private lateinit var adapter: ClienteAdapter
    private var listaCompleta = mutableListOf<Cliente>()

    private val registrarResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        cargarClientes() // 🔄 Refresca al volver del formulario
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        setupToolbar()
        initViews()
        setupAdapter()
        setupSearch()
        setupFab()

        cargarClientes()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(getDrawable(android.R.drawable.ic_menu_revert))
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews() {
        etSearchClientes = findViewById(R.id.etSearchClientes)
        rvClientes = findViewById(R.id.rvClientes)
        fabAgregarCliente = findViewById(R.id.fabAgregarCliente)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupAdapter() {
        adapter = ClienteAdapter(
            onEdit = { cliente -> abrirFormulario(cliente) },
            onDelete = { cliente -> confirmarEliminar(cliente) }
        )
        rvClientes.layoutManager = LinearLayoutManager(this)
        rvClientes.adapter = adapter
    }

    private fun setupSearch() {
        etSearchClientes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
        })
    }

    private fun setupFab() {
        fabAgregarCliente.setOnClickListener {
            abrirFormulario(null) // 🆕 Crear nuevo
        }
    }

    private fun cargarClientes() {
        lifecycleScope.launch {
            try {
                if (!ApiClient.isReady) {
                    Toast.makeText(this@ClientesActivity, "⚠️ Conexión API no lista", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val resp = ApiClient.apiService.obtenerClientes()

                if (resp.success && resp.data != null) {
                    // ✅ GUARDAR EN LA LISTA COMPLETA PARA PODER FILTRAR
                    listaCompleta.clear()
                    listaCompleta.addAll(resp.data)

                    adapter.submitList(resp.data)

                    // ✅ Mostrar/Ocultar Estado Vacío
                    if (resp.data.isEmpty()) {
                        llEmptyState.visibility = LinearLayout.VISIBLE
                        rvClientes.visibility = RecyclerView.GONE
                    } else {
                        llEmptyState.visibility = LinearLayout.GONE
                        rvClientes.visibility = RecyclerView.VISIBLE
                    }
                } else {
                    Toast.makeText(this@ClientesActivity, resp.message ?: "No hay clientes", Toast.LENGTH_SHORT).show()
                    llEmptyState.visibility = LinearLayout.VISIBLE
                    rvClientes.visibility = RecyclerView.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        val filtrada = listaCompleta.filter { cliente ->
            cliente.nombre?.contains(query, ignoreCase = true) == true ||
                    cliente.correo?.contains(query, ignoreCase = true) == true ||
                    cliente.telefono?.contains(query, ignoreCase = true) == true
        }
        adapter.submitList(filtrada)
    }

    private fun abrirFormulario(cliente: Cliente?) {
        val intent = Intent(this, FormClienteActivity::class.java).apply {
            putExtra("cliente_extra", cliente)
        }
        registrarResultado.launch(intent)
    }

    private fun confirmarEliminar(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar cliente?")
            .setMessage("Se eliminará permanentemente a ${cliente.nombre}.")
            .setPositiveButton("Sí") { _, _ -> eliminarCliente(cliente) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCliente(cliente: Cliente) {
        lifecycleScope.launch {
            try {
                // 1. Hacemos la petición
                val response = ApiClient.apiService.eliminarCliente(cliente.id?.toString() ?: "")

                // 2. Verificamos si la respuesta HTTP fue exitosa (200 OK, etc.)
                if (response.isSuccessful) {
                    // 3. Obtenemos el cuerpo de la respuesta (tu objeto ApiResponse)
                    val apiResp = response.body()

                    // 4. Verificamos el estado lógico de tu API
                    if (apiResp?.success == true) {
                        Toast.makeText(this@ClientesActivity, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                        cargarClientes() // Recarga la lista
                    } else {
                        // La petición HTTP funcionó, pero la API devolvió success: false
                        Toast.makeText(this@ClientesActivity, apiResp?.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error HTTP (404, 500, etc.)
                    Toast.makeText(this@ClientesActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}