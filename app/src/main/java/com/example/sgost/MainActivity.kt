package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Referenciar vistas (usando ? para evitar crash si el ID no existe)
        val tvAdminName = findViewById<TextView>(R.id.tvAdminName)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val cardDashboard = findViewById<MaterialCardView>(R.id.cardDashboard)
        val cardUsuarios = findViewById<MaterialCardView>(R.id.cardUsuarios)
        val cardVentas = findViewById<MaterialCardView>(R.id.cardVentas)
        val cardReportes = findViewById<MaterialCardView>(R.id.cardReportes)
        val cardConfig = findViewById<MaterialCardView>(R.id.cardConfig)
        val cardSoporte = findViewById<MaterialCardView>(R.id.cardSoporte)

        // 2. Cargar nombre del admin
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        val adminNombre = prefs.getString("admin_nombre", "Administrador") ?: "Administrador"

        // Usamos ? para que no falle si tvAdminName es null
        tvAdminName?.text = "Hola, $adminNombre"

        // 3. Botón Logout
        btnLogout?.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 4. Clicks de tarjetas (Ejemplos)
        cardDashboard?.setOnClickListener { Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show() }
        cardUsuarios?.setOnClickListener { Toast.makeText(this, "Usuarios", Toast.LENGTH_SHORT).show() }
        cardVentas?.setOnClickListener { Toast.makeText(this, "Ventas", Toast.LENGTH_SHORT).show() }
        cardReportes?.setOnClickListener { Toast.makeText(this, "Reportes", Toast.LENGTH_SHORT).show() }
        cardConfig?.setOnClickListener { Toast.makeText(this, "Config", Toast.LENGTH_SHORT).show() }
        cardSoporte?.setOnClickListener { Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show() }
    }
}