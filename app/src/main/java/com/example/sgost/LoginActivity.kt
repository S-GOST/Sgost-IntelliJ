package com.example.sgost

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiClient
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isNotEmpty() && password.isNotEmpty()) {
                login(usuario, password)
            } else {
                Toast.makeText(this, "⚠️ Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Versión corregida con lifecycleScope
    private fun login(usuario: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando..."

        lifecycleScope.launch {
            try {
                val request = LoginRequest(usuario, password)
                val response: LoginResponse = ApiClient.apiService.loginAdmin(request)

                // Guardar token y datos de sesión
                val prefs = getSharedPreferences("sgost_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("auth_token", response.token)
                    putString("user_name", response.nombre)
                    apply()
                }

                Toast.makeText(this@LoginActivity, "✅ Bienvenido ${response.nombre}", Toast.LENGTH_SHORT).show()

                // Ir al MainActivity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                btnLogin.isEnabled = true
                btnLogin.text = "INICIAR SESIÓN"
            }
        }
    }
}