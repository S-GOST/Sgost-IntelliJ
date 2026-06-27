package com.example.sgost

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Cliente
import com.example.sgost.model.Moto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class FormClienteActivity : AppCompatActivity() {

    private lateinit var tvFormTitle: TextView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etTipoDoc: TextInputEditText
    private lateinit var etUbicacion: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private lateinit var etPlaca: TextInputEditText
    private lateinit var etModelo: TextInputEditText
    private lateinit var etMarca: TextInputEditText
    private lateinit var etRecorrido: TextInputEditText

    private var clienteEditar: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_clientes)

        setupToolbar()
        initViews()
        cargarModoEdicion()
        setupListeners()
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar)?.let {
            setSupportActionBar(it)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "Gestión de Cliente"
            }
        }
    }

    private fun initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle)
        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        etTipoDoc = findViewById(R.id.etTipoDoc)
        etUbicacion = findViewById(R.id.etUbicacion)
        layoutPassword = findViewById(R.id.layoutPassword)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)

        etPlaca = findViewById(R.id.etPlaca)
        etModelo = findViewById(R.id.etModelo)
        etMarca = findViewById(R.id.etMarca)
        etRecorrido = findViewById(R.id.etRecorrido)
    }

    private fun cargarModoEdicion() {
        clienteEditar = intent.getParcelableExtra("cliente_extra")
        if (clienteEditar != null) {
            val c = clienteEditar!!
            tvFormTitle.text = "Editar Cliente"
            btnGuardar.text = "ACTUALIZAR"
            layoutPassword.visibility = View.GONE
            etNombre.setText(c.nombre)
            etUsuario.setText(c.usuario)
            etCorreo.setText(c.correo)
            etTelefono.setText(c.telefono)
            etTipoDoc.setText(c.tipoDocumento)
            etUbicacion.setText(c.ubicacion)
        } else {
            tvFormTitle.text = "Crear Cliente"
            btnGuardar.text = "GUARDAR TODO"
            layoutPassword.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener { guardarDatosIntegrales() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun guardarDatosIntegrales() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val placa = etPlaca.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "❌ Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        if (clienteEditar == null && contrasena.isEmpty()) {
            Toast.makeText(this, "❌ La contraseña es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }
        if (placa.isEmpty()) {
            Toast.makeText(this, "❌ La placa de la moto es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnCancelar.isEnabled = false
        btnGuardar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val clienteData = if (clienteEditar != null) {
                    clienteEditar!!.copy(
                        nombre = nombre,
                        usuario = etUsuario.text.toString().trim(),
                        correo = correo,
                        telefono = etTelefono.text.toString().trim(),
                        tipoDocumento = etTipoDoc.text.toString().trim(),
                        ubicacion = etUbicacion.text.toString().trim()
                    )
                } else {
                    Cliente(
                        id = null,
                        ubicacion = etUbicacion.text.toString().trim(),
                        nombre = nombre,
                        usuario = etUsuario.text.toString().trim(),
                        contrasena = contrasena,
                        tipoDocumento = etTipoDoc.text.toString().trim(),
                        correo = correo,
                        telefono = etTelefono.text.toString().trim()
                    )
                }

                val idClienteFinal = if (clienteEditar != null) {
                    clienteEditar!!.id
                } else {
                    val responseCliente = ApiAndroid.apiService.registrarCliente(clienteData)
                    if (!responseCliente.isSuccessful || responseCliente.body()?.success != true) {
                        throw Exception("Error al crear el cliente: ${responseCliente.body()?.message}")
                    }

                    // ✅ CORREGIDO: Extraemos el ID del objeto ya convertido por Retrofit
                    val data = responseCliente.body()?.data
                    val insertId = when (data) {
                        is Map<*, *> -> (data["insertId"] as? Int) ?: (data["id"] as? Int)
                        is Cliente  -> data.id
                        else        -> -1
                    }

                    if (insertId == -1) {
                        throw Exception("El servidor no devolvió un ID válido para el cliente.")
                    }
                    Log.d("API_CLIENTE", "✅ ID extraído correctamente: $insertId")
                    insertId
                }

                // 4. Guardar Moto
                val motoData = Moto(
                    idMotos = null,
                    idClientes = idClienteFinal,
                    placa = placa,
                    modelo = etModelo.text.toString().trim(),
                    marca = etMarca.text.toString().trim(),
                    recorrido = etRecorrido.text.toString().trim() // String como requiere tu modelo
                )

                Log.d("API_MOTO", "🚀 Creando moto para cliente ID: $idClienteFinal")
                val responseMoto = ApiAndroid.apiService.crearMoto(motoData)

                if (responseMoto.isSuccessful && responseMoto.body()?.success == true) {
                    Toast.makeText(this@FormClienteActivity, "✅ Cliente y Moto registrados correctamente", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val msg = responseMoto.body()?.message ?: responseMoto.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(this@FormClienteActivity, "✅ Cliente creado, pero ❌ Error moto: $msg", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("FORM_CLIENTE", "Error crítico: ${e.message}", e)
                Toast.makeText(this@FormClienteActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                if (!isFinishing) {
                    btnGuardar.isEnabled = true
                    btnCancelar.isEnabled = true
                    btnGuardar.text = if (clienteEditar != null) "ACTUALIZAR" else "GUARDAR TODO"
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}