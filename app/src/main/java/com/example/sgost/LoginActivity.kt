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

    private fun llamarApiLogin(usuario: String, contrasena: String) {
        val request = LoginRequest(usuario, contrasena)

        ApiClient.apiService.loginAdmin(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                btnLogin.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // ✅ Tu backend ya valida y devuelve rol: "admin"
                    if (data.success && data.rol == "admin") {
                        guardarToken(data.token)
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
                btnLogin.isEnabled = true
                showErrorMessage("Error de red: Verifica IP y puerto del backend")
                android.util.Log.e("API_ERROR", t.message ?: "Desconocido", t)
            }
        })
    }

    private fun guardarToken(token: String) {
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        prefs.edit().putString("jwt_token", token).apply()
    }

    private fun showErrorMessage(message: String) {
        tvMessage.text = message
        tvMessage.visibility = TextView.VISIBLE
    }
}