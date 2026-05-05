package com.example.sgost.loginActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sgost.MainActivity
import com.example.sgost.R
import com.example.sgost.api.ApiClient
import com.example.sgost.model.LoginRequest
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

        // Referencias a vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvMessage = findViewById(R.id.tvMessage)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showErrorMessage("Por favor, completa todos los campos")
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            tvMessage.visibility = TextView.GONE
            llamarApiLogin(email, password)
        }
    }

    private fun llamarApiLogin(email: String, password: String) {
        val request = LoginRequest(email, password)

        ApiClient.apiService.login(request).enqueue(object : Callback<com.example.sgost.model.LoginResponse> {
            override fun onResponse(call: Call<com.example.sgost.model.LoginResponse>, response: Response<com.example.sgost.model.LoginResponse>) {
                btnLogin.isEnabled = true
                if (response.isSuccessful && response.body() != null) {
                    // ✅ ÉXITO: Navegar a MainActivity y guardar token si lo necesitas
                    val token = response.body()!!.token
                    Toast.makeText(this@LoginActivity, "Login exitoso: $token", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // ❌ Error del servidor (401, 400, 500, etc.)
                    showErrorMessage("Credenciales incorrectas o error del servidor (${response.code()})")
                }
            }

            override fun onFailure(call: Call<com.example.sgost.model.LoginResponse>, t: Throwable) {
                // ❌ Error de red / conexión
                btnLogin.isEnabled = true
                showErrorMessage("Error de conexión: Verifica que tu backend esté encendido y accesible.")
                android.util.Log.e("API_ERROR", t.message ?: "Desconocido", t)
            }
        })
    }

    private fun showErrorMessage(message: String) {
        tvMessage.text = message
        tvMessage.visibility = TextView.VISIBLE
    }
}