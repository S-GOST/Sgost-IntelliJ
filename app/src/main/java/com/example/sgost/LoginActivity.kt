package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.LoginRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var rgTipoUsuario: RadioGroup
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvMessage: TextView
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        rgTipoUsuario = findViewById(R.id.rgTipoUsuario)
        etUsuario = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvMessage = findViewById(R.id.tvMessage)

        rgTipoUsuario.check(R.id.rbAdmin) // Selecciona Admin por defecto
        btnLogin.setOnClickListener { iniciarSesionUnificado() }
    }

    private fun iniciarSesionUnificado() {
        val usuario = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("⚠️ Ingrese usuario/correo y contraseña")
            return
        }

        val tipoSeleccionado = when(rgTipoUsuario.checkedRadioButtonId) {
            R.id.rbAdmin   -> "admin"
            R.id.rbTecnico -> "tecnico"
            R.id.rbCliente -> "cliente"
            else -> return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Ingresando..."
        tvMessage.visibility = TextView.GONE

        val request = LoginRequest(usuario = usuario, contrasena = password)

        lifecycleScope.launch {
            try {
                val response = when(tipoSeleccionado) {
                    "admin"   -> ApiClient.apiService.loginAdmin(request)
                    "tecnico" -> ApiClient.apiService.loginTecnico(request)
                    "cliente" -> ApiClient.apiService.loginCliente(request)
                    else -> throw IllegalStateException("Tipo no válido")
                }

                if (response.success) {
                    prefs.edit {
                        putString("auth_token", response.token ?: "")
                        putString("user_nombre", response.nombre ?: "Usuario")
                        putString("user_type", tipoSeleccionado)

                        // 👇 CAMBIADO: Usar minúsculas para coincidir con tu modelo LoginResponse
                        putString("user_correo", response.correo ?: "")
                        putString("user_telefono", response.telefono ?: "")
                        putString("user_ubicacion", response.ubicacion ?: "")
                    }

                    Toast.makeText(this@LoginActivity, "✅ Bienvenido ${response.nombre}", Toast.LENGTH_SHORT).show()

                    val targetActivity = when(tipoSeleccionado) {
                        "admin"   -> DasbohadActivityAdmin::class.java
                        "tecnico" -> DashboardActivityTecnicos::class.java
                        "cliente" -> DashboardActivityClientes::class.java
                        else -> throw IllegalStateException("Dashboard no configurado")
                    }

                    startActivity(Intent(this@LoginActivity, targetActivity))
                    finish()
                } else {
                    mostrarMensaje("❌ ${response.message ?: "Credenciales incorrectas"}")
                }
            } catch (e: Exception) {
                mostrarMensaje("⚠️ Error: ${e.message}")
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "INICIAR SESIÓN"
            }
        }
    }

    private fun mostrarMensaje(msg: String) {
        tvMessage.text = msg
        tvMessage.visibility = TextView.VISIBLE
    }
}