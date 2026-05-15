package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.LoginRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivityAdmin : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvMessage: TextView
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvMessage = findViewById(R.id.tvMessage)

        btnLogin.setOnClickListener { iniciarSesion() }
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            mostrarMensaje("⚠️ Ingrese correo y contraseña")
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Ingresando..."

        val request = LoginRequest(usuario = email, contrasena = password)

        // 🟢 Llamada Suspendida dentro de una Coroutine
        lifecycleScope.launch {
            try {
                // Se ejecuta en hilo de fondo automáticamente
                val response = withContext(Dispatchers.IO) {
                    // ⚠️ Si tu ApiClient usa 'apiService' en vez de 'api', cámbialo aquí
                    ApiClient.apiService.loginAdmin(request)
                }

                if (response.success) {
                    prefs.edit {
                        putString("auth_token", response.token ?: "")
                        putString("admin_nombre", response.nombre ?: "Admin")
                    }

                    Toast.makeText(this@LoginActivityAdmin, "✅ Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivityAdmin, DasbohadActivityAdmin::class.java))
                    finish()
                } else {
                    mostrarMensaje("❌ ${response.message ?: "Credenciales incorrectas"}")
                }
            } catch (e: Exception) {
                mostrarMensaje("⚠️ Error de red: ${e.message}")
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