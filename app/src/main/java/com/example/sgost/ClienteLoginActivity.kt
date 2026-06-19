package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.RetrofitClient
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ClienteLoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val usuario = etEmail.text?.toString()?.trim() ?: ""
            val contrasena = etPassword.text?.toString()?.trim() ?: ""

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "⚠️ Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            validarClienteAsync(usuario, contrasena)
        }
    }

    private fun validarClienteAsync(usuario: String, contrasena: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Verificando..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.loginCliente(
                    LoginRequest(usuario = usuario, contrasena = contrasena)
                )

                val userId = response.id ?: 0

                if (response.success == true && userId > 0) {
                    guardarSesionCliente(userId)

                    // 💡 Opcional: Guarda el token para peticiones futuras
                    getSharedPreferences("sgost_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("user_id", userId)
                        .commit() // 🔥 Sincrono: evita que MainActivity lea 0 y te envíe de vuelta

                    startActivity(Intent(this@ClienteLoginActivity, WelcomeActivity::class.java))
                    finish()
                } else {
                    val msg = response.message ?: "❌ Credenciales incorrectas"
                    Toast.makeText(this@ClienteLoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ClienteLoginActivity, "❌ Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "Iniciar Sesión"
            }
        }
    }

    private fun guardarSesionCliente(userId: Int) {
        getSharedPreferences("sgost_prefs", MODE_PRIVATE)
            .edit()
            .putInt("user_id", userId)
            .apply()
    }
}