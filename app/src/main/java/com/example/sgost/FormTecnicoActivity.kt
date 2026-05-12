package com.example.sgost

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.ApiResponse
import com.example.sgost.model.Tecnico
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class FormTecnicoActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var layoutConfirmPassword: TextInputLayout
    private lateinit var etTipoDoc: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var btnGuardar: MaterialButton
    private lateinit var tvError: TextView

    private var isEditMode = false
    private var tecnicoExistente: Tecnico? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tecnico)

        setupViews()
        checkEditMode()
        setupButtons()
    }

    private fun setupViews() {
        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        layoutPassword = findViewById(R.id.layoutPassword)
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword)
        etTipoDoc = findViewById(R.id.etTipoDocumento)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        btnGuardar = findViewById(R.id.btnGuardar)
        tvError = findViewById(R.id.tvError)
    }

    private fun checkEditMode() {
        isEditMode = intent.getBooleanExtra("IS_EDIT", false)
        if (isEditMode) {
            tecnicoExistente = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("TECNICO_DATA", Tecnico::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("TECNICO_DATA") as? Tecnico
            }

            tecnicoExistente?.let {
                etNombre.setText(it.nombre)
                etUsuario.setText(it.usuario)
                etTipoDoc.setText(it.tipoDocumento)
                etCorreo.setText(it.correo)
                etTelefono.setText(it.telefono)
            }
            layoutPassword.visibility = View.GONE
            layoutConfirmPassword.visibility = View.GONE
            btnGuardar.text = "ACTUALIZAR"
        } else {
            findViewById<TextView>(R.id.tvFormTitle).text = "Nuevo Técnico"
        }
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener { finish() }
        btnGuardar.setOnClickListener { if (validarFormulario()) guardarTecnico() }
    }

    private fun validarFormulario(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val usuario = etUsuario.text.toString().trim()
        val tipoDoc = etTipoDoc.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirmPass = etConfirmPassword.text.toString()

        if (nombre.isEmpty() || usuario.isEmpty() || tipoDoc.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
            mostrarError("⚠️ Todos los campos son obligatorios")
            return false
        }
        if (!isEditMode) {
            if (pass.length < 6) {
                mostrarError("⚠️ La contraseña debe tener ≥ 6 caracteres")
                return false
            }
            if (pass != confirmPass) {
                mostrarError("⚠️ Las contraseñas no coinciden")
                return false
            }
        }
        tvError.visibility = View.GONE
        return true
    }

    private fun mostrarError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun restaurarBoton() {
        btnGuardar.isEnabled = true
        btnGuardar.text = if (isEditMode) "ACTUALIZAR" else "GUARDAR"
    }

    private fun guardarTecnico() {
        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        val prefs = getSharedPreferences("sgost_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", "") ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"

        lifecycleScope.launch {
            try {
                val tecnico = Tecnico(
                    id = tecnicoExistente?.id ?: "",
                    nombre = etNombre.text.toString().trim(),
                    usuario = etUsuario.text.toString().trim(),
                    contrasena = if (!isEditMode) etPassword.text.toString() else tecnicoExistente?.contrasena ?: "",
                    tipoDocumento = etTipoDoc.text.toString().trim(),
                    correo = etCorreo.text.toString().trim(),
                    telefono = etTelefono.text.toString().trim()
                )

                val response = if (isEditMode) {
                    ApiClient.apiService.actualizarTecnico(authHeader, tecnico.id, tecnico)
                } else {
                    ApiClient.apiService.crearTecnico(authHeader, tecnico)
                }

                if (response.success) {
                    Toast.makeText(this@FormTecnicoActivity, "✅ ${response.message}", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    mostrarError("❌ ${response.message}")
                    restaurarBoton()
                }
            } catch (e: Exception) {
                mostrarError("❌ Error de red: ${e.message}")
                restaurarBoton()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}