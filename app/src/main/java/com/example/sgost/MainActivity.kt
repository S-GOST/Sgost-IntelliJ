package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Botón Iniciar Sesión
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 🔹 Botón Ir a Formulario de Cliente
        val btnIrForm = findViewById<MaterialButton>(R.id.btnRegistro)
        btnIrForm.setOnClickListener {
            startActivity(Intent(this, FormClienteActivity::class.java))
        }
    }
}