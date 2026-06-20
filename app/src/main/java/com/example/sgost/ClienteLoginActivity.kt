package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.util.Base64
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
import org.json.JSONObject

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

                Log.d("LoginDebug", "✅ Respuesta: success=${response.success}, id=${response.id}, token=${response.token?.take(10)}...")

                // 🟢 SOLUCIÓN: Solo validamos success == true
                // El backend responde 200 OK con success:true pero NO incluye 'id' en el JSON.
                if (response.success == true) {
                    // Extrae el ID del token JWT si el backend no lo envió explícitamente
                    val userId = response.id ?: extractIdFromJwt(response.token)

                    // 💾 Guardamos sesión de forma segura y SÍNCRONA
                    getSharedPreferences("sgost_prefs", MODE_PRIVATE).edit().apply {
                        putInt("user_id", userId ?: 0)
                        putString("token", response.token)
                        putString("nombre", response.nombre)
                        putString("rol", response.rol)
                        commit() // commit() es síncrono: garantiza que WelcomeActivity lea los datos inmediatamente
                    }

                    // 🚀 Redirección inmediata
                    startActivity(Intent(this@ClienteLoginActivity, WelcomeActivity::class.java))
                    finish()
                } else {
                    val msg = response.message ?: "❌ Credenciales incorrectas"
                    Toast.makeText(this@ClienteLoginActivity, msg, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("LoginDebug", "❌ Error en red", e)
                val msg = when (e) {
                    is retrofit2.HttpException -> "❌ Error HTTP ${e.code()}"
                    is java.net.SocketTimeoutException -> "⏳ Tiempo de espera agotado"
                    is java.net.ConnectException -> "🌐 Sin conexión a internet"
                    else -> "❌ ${e.message}"
                }
                Toast.makeText(this@ClienteLoginActivity, msg, Toast.LENGTH_SHORT).show()
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "Iniciar Sesión"
            }
        }
    }

    // 🆛 Utilidad: Extrae el `id` del payload del JWT sin librerías externas
    private fun extractIdFromJwt(token: String?): Int? {
        return try {
            val parts = token?.split(".")
            if (parts?.size == 3) {
                // El payload es la parte central del JWT (base64url)
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING))
                val json = JSONObject(payload)
                if (json.has("id")) json.getInt("id") else null
            } else null
        } catch (e: Exception) {
            Log.e("JwtDecode", "No se pudo leer el ID del token", e)
            null
        }
    }

    // Método mantenido por compatibilidad, pero ya no se usa internamente
    private fun guardarSesionCliente(userId: Int) {
        // La sesión ahora se guarda directamente en validarClienteAsync con commit()
    }
}