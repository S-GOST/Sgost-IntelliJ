package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sgost.api.ApiClient
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvMessage = findViewById(R.id.tvMessage)

        btnLogin.setOnClickListener {
            val usuario = etEmail.text.toString().trim()
            val contrasena = etPassword.text.toString().trim()

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                showErrorMessage("Por favor, completa todos los campos")
                return@setOnClickListener
            }

            // 🟡 UI: Deshabilitar botón y cambiar texto
            btnLogin.isEnabled = false
            btnLogin.text = "CARGANDO..."
            tvMessage.visibility = TextView.GONE
            llamarApiLogin(usuario, contrasena)
        }
    }

    private fun llamarApiLogin(usuario: String, contrasena: String) {
        val request = LoginRequest(usuario, contrasena)

        ApiClient.apiService.loginAdmin(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // 🔙 Restaurar botón
                btnLogin.isEnabled = true
                btnLogin.text = "INICIAR SESIÓN"

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // ✅ Tu backend valida y devuelve rol: "admin"
                    if (data.success && data.rol == "admin") {
                        guardarSesion(data.token, data.nombre)
                        Toast.makeText(this@LoginActivity, "Bienvenido, ${data.nombre}", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showErrorMessage(data.message.ifEmpty { "Credenciales incorrectas" })
                    }
                } else {
                    showErrorMessage("Error del servidor (${response.code()})")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // 🔙 Restaurar botón en caso de error
                btnLogin.isEnabled = true
                btnLogin.text = "INICIAR SESIÓN"
                showErrorMessage("Error de red: Verifica IP y puerto del backend")
                android.util.Log.e("API_ERROR", t.message ?: "Desconocido", t)
            }
        })
    }

    // 💾 Guarda Token Y Nombre para usarlos en MainActivity
    private fun guardarSesion(token: String, nombre: String) {
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("jwt_token", token)
            .putString("admin_nombre", nombre)
            .apply()
    }

    private fun showErrorMessage(message: String) {
        tvMessage.text = message
        tvMessage.visibility = TextView.VISIBLE
    }
}