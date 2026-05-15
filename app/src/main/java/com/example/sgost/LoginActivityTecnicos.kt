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

class LoginActivityTecnicos : AppCompatActivity() {

    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvMessage: TextView
    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_tecnicos)

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvMessage = findViewById(R.id.tvMessage)

        btnLogin.setOnClickListener { iniciarSesionTecnico() }
    }

    private fun iniciarSesionTecnico() {
        val usuario = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("⚠️ Ingrese usuario y contraseña")
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Ingresando..."
        tvMessage.visibility = TextView.GONE

        val request = LoginRequest(usuario = usuario, contrasena = password)

        lifecycleScope.launch {
            try {
                // 🟢 Retrofit suspend ya corre en IO, no hace falta withContext
                val response = ApiClient.apiService.loginTecnico(request)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        prefs.edit {
                            putString("auth_token", response.token ?: "")
                            putString("tecnico_nombre", response.nombre ?: "Técnico")
                            putString("user_type", "tecnico")
                        }

                        Toast.makeText(this@LoginActivityTecnicos, "✅ Bienvenido ${response.nombre}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivityTecnicos, DashboardActivityTecnicos::class.java))
                        finish() // 👈 Se ejecuta seguro en Main
                    } else {
                        mostrarMensaje("❌ ${response.message ?: "Credenciales incorrectas"}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarMensaje("⚠️ Error de red: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "INICIAR SESIÓN"
                }
            }
        }
    }

    private fun mostrarMensaje(msg: String) {
        tvMessage.text = msg
        tvMessage.visibility = TextView.VISIBLE
    }
}