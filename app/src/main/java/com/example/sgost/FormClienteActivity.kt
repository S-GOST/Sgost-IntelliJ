package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Cliente
import kotlinx.coroutines.launch

class FormClienteActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etTipoDoc: TextInputEditText
    private lateinit var etUbicacion: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_clientes)

        setupToolbar()
        initViews()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar? = findViewById(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "Gestión de Cliente"
            }
            it.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        etTipoDoc = findViewById(R.id.etTipoDoc)
        etUbicacion = findViewById(R.id.etUbicacion)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener { guardarCliente() }
    }

    private fun guardarCliente() {
        val nombre = etNombre.text.toString().trim()
        val usuario = etUsuario.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val tipoDoc = etTipoDoc.text.toString().trim()
        val ubicacion = etUbicacion.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "❌ Nombre, correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                // 1. Construir el objeto Cliente
                val nuevoCliente = Cliente(
                    nombre = nombre,
                    usuario = usuario,
                    contrasena = contrasena,
                    correo = correo,
                    telefono = telefono,
                    tipoDocumento = tipoDoc,
                    ubicacion = ubicacion
                )

                // 2. Llamar a la API (ajusta el nombre del método según tu ApiService)
                val response = ApiClient.apiService.registrarCliente(nuevoCliente)

                if (response.isSuccessful) {
                    Toast.makeText(this@FormClienteActivity, "✅ Cliente registrado correctamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(this@FormClienteActivity, "❌ Error servidor: $errorBody", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@FormClienteActivity, "❌ Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (!isFinishing) {
                    btnGuardar.isEnabled = true
                    btnGuardar.text = "GUARDAR CLIENTE"
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}