package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Administrador
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class FormAdministradorActivity : AppCompatActivity() {

    private lateinit var tvFormTitle: TextView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etTipoDoc: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var layoutConfirmPassword: TextInputLayout
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var adminEditar: Administrador? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_administrador)

        setupToolbar()
        initViews()
        cargarModoEdicion()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar? = findViewById(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "Gestión de Administrador"
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
        layoutPassword = findViewById(R.id.layoutPassword)
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun cargarModoEdicion() {
        adminEditar = intent.getParcelableExtra("admin_extra")

        if (adminEditar != null) {
            val a = adminEditar!!
            tvFormTitle.text = "Editar Administrador"
            btnGuardar.text = "ACTUALIZAR"

            layoutPassword.visibility = View.GONE
            layoutConfirmPassword.visibility = View.GONE

            etNombre.setText(a.nombre)
            etUsuario.setText(a.usuario)
            etCorreo.setText(a.correo)
            etTelefono.setText(a.telefono)
            etTipoDoc.setText(a.tipoDocumento)
        } else {
            tvFormTitle.text = "Crear Administrador"
            btnGuardar.text = "GUARDAR"
            layoutPassword.visibility = View.VISIBLE
            layoutConfirmPassword.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener { guardarAdministrador() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun guardarAdministrador() {
        val nombre = etNombre.text.toString().trim()
        val usuario = etUsuario.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val tipoDoc = etTipoDoc.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "❌ Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        if (adminEditar == null && contrasena.isEmpty()) {
            Toast.makeText(this, "❌ La contraseña es obligatoria para registrar", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnCancelar.isEnabled = false
        btnGuardar.text = if (adminEditar != null) "Actualizando..." else "Guardando..."

        lifecycleScope.launch {
            try {
                if (adminEditar != null) {
                    val adminActualizar = adminEditar!!.copy(
                        nombre = nombre,
                        usuario = usuario,
                        correo = correo,
                        telefono = telefono,
                        tipoDocumento = tipoDoc,
                    )
                    // 👇 Tu API retorna ApiResponse<Tecnico> (posible typo en backend, pero funciona)
                    val response = ApiAndroid.apiService.actualizarAdministradores(
                        adminActualizar.id.toString(),
                        adminActualizar
                    )

                    if (response.success) {
                        Toast.makeText(this@FormAdministradorActivity, "✅ Administrador actualizado correctamente", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@FormAdministradorActivity, "❌ ${response.message ?: "Error al actualizar"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val nuevoAdmin = Administrador(
                        nombre = nombre,
                        usuario = usuario,
                        contrasena = contrasena,
                        correo = correo,
                        telefono = telefono,
                        tipoDocumento = tipoDoc
                    )
                    val response = ApiAndroid.apiService.crearAdministradores(nuevoAdmin)

                    if (response.success) {
                        Toast.makeText(this@FormAdministradorActivity, "✅ Administrador registrado exitosamente", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@FormAdministradorActivity, "❌ ${response.message ?: "Error al registrar"}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FormAdministradorActivity, "❌ Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (!isFinishing) {
                    btnGuardar.isEnabled = true
                    btnCancelar.isEnabled = true
                    btnGuardar.text = if (adminEditar != null) "ACTUALIZAR" else "GUARDAR"
                }
            }
        }
    }

    // ✅ Manejo oficial del botón de retroceso de la Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}