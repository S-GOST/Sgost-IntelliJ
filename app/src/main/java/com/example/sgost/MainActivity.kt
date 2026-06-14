package com.example.sgost

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Botón Iniciar Sesión
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 🔹 Botón Registrarse
        findViewById<MaterialButton>(R.id.btnRegistro).setOnClickListener {
            startActivity(Intent(this, FormClienteActivity::class.java))
        }

        // 🔹 Botón Ir al Catálogo
        findViewById<MaterialButton>(R.id.btnIrCatalogo).setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
        }
    }
}