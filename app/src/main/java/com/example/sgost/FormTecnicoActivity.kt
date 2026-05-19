package com.example.sgost

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.Tecnico
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class FormTecnicoActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etTipoDoc: TextInputEditText
    private lateinit var etDocumento: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var layoutConfirmPassword: TextInputLayout
    private lateinit var tvError: TextView
    private lateinit var btnGuardar: MaterialButton
    private lateinit var tvFormTitle: TextView

    private var tecnicoEditar: Tecnico? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tecnico)

        setupToolbar()
        bindViews()
        cargarModoEdicion()
        configurarBoton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(getDrawable(android.R.drawable.ic_menu_revert))
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun bindViews() {
        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etTipoDoc = findViewById(R.id.etTipoDoc)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        layoutPassword = findViewById(R.id.layoutPassword)
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword)
        tvError = findViewById(R.id.tvError)
        btnGuardar = findViewById(R.id.btnGuardar)
        tvFormTitle = findViewById(R.id.tvFormTitle)
    }

    private fun cargarModoEdicion() {
        // ✅ Compatibilidad API 33+ (Android 13+)
        tecnicoEditar = intent.getParcelableExtra("tecnico_extra", Tecnico::class.java)
        if (tecnicoEditar != null) {
            val t = tecnicoEditar!!
            tvFormTitle.text = "Editar Técnico"
            btnGuardar.text = "ACTUALIZAR"
            layoutPassword.visibility = View.GONE
            layoutConfirmPassword.visibility = View.GONE

            etNombre.setText(t.nombre)
            etUsuario.setText(t.usuario)
            etTipoDoc.setText(t.tipoDocumento)
            etCorreo.setText(t.correo)
            etTelefono.setText(t.telefono)
        }
    }

    private fun configurarBoton() {
        findViewById<Button>(R.id.btnCancelar)?.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            if (!validarFormulario()) return@setOnClickListener

            btnGuardar.isEnabled = false
            btnGuardar.text = if (tecnicoEditar != null) "Actualizando..." else "Guardando..."
            tvError.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val tecnico = Tecnico(
                        idTecnicos = tecnicoEditar?.idTecnicos,
                        nombre = etNombre.text.toString().trim(),
                        usuario = etUsuario.text.toString().trim(),
                        contrasena = if (tecnicoEditar == null) etPassword.text.toString().trim() else null,
                        tipoDocumento = etTipoDoc.text.toString().trim(),
                        correo = etCorreo.text.toString().trim(),
                        telefono = etTelefono.text.toString().trim()
                    )

                    val resp = if (tecnicoEditar != null) {
                        val id = tecnicoEditar!!.idTecnicos?.toString() ?: return@launch
                        ApiClient.apiService.actualizarTecnico(id, tecnico)
                    } else {
                        ApiClient.apiService.crearTecnico(tecnico)
                    }

                    if (resp.success) {
                        Toast.makeText(this@FormTecnicoActivity, "✅ Operación exitosa", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        mostrarError(resp.message ?: "Error en el servidor")
                    }
                } catch (e: Exception) {
                    mostrarError("Error de red: ${e.message}")
                } finally {
                    if (!isFinishing) {
                        btnGuardar.isEnabled = true
                        btnGuardar.text = if (tecnicoEditar != null) "ACTUALIZAR" else "GUARDAR"
                    }
                }
            }
        }
    }

    private fun validarFormulario(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val usuario = etUsuario.text.toString().trim()
        val correo = etCorreo.text.toString().trim()

        if (nombre.isEmpty() || usuario.isEmpty() || correo.isEmpty()) {
            mostrarError("Completa los campos obligatorios.")
            return false
        }

        if (tecnicoEditar == null) {
            val p = etPassword.text.toString().trim()
            val cp = etConfirmPassword.text.toString().trim()
            if (p.isEmpty()) { mostrarError("La contraseña es obligatoria."); return false }
            if (p != cp) { mostrarError("Las contraseñas no coinciden."); return false }
        }
        return true
    }

    private fun mostrarError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }
}