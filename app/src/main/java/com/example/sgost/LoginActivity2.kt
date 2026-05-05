package com.example.sgost.loginActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sgost.R
import com.example.sgost.ClientesActivity
import com.example.sgost.api.ApiClient
import com.example.sgost.api.ApiService
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val message = findViewById<TextView>(R.id.message)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                val api = ApiClient.apiService
                val request = LoginRequest(email, password)

                api.login(request).enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {

                            val token = response.body()?.token

                            // ✅ guardar token
                            val prefs = getSharedPreferences("app", MODE_PRIVATE)
                            prefs.edit().putString("token", token).apply()

                            message.setTextColor(Color.GREEN)
                            message.text = "Ingreso con éxito"

                            val intent = Intent(this@LoginActivity2, ClientesActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            message.setTextColor(Color.RED)
                            message.text = "Credenciales incorrectas"
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        message.setTextColor(Color.RED)
                        message.text = t.message
                        t.printStackTrace()
                    }
                })

            } else {
                message.setTextColor(Color.RED)
                message.text = "Campos vacíos"
            }
        }
    }
}