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

        // 1. Referenciar vistas con los NUEVOS IDs del XML
        val tvAdminName = findViewById<TextView>(R.id.tvAdminName)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Tarjetas del Menú (IDs actualizados)
        val cardDashboard = findViewById<MaterialCardView>(R.id.cardDashboard)
        val cardTecnicos = findViewById<MaterialCardView>(R.id.cardTecnicos)       // <-- Cambiado de cardUsuarios
        val cardOrdenes = findViewById<MaterialCardView>(R.id.cardOrdenes)         // <-- Cambiado de cardVentas
        val cardInformes = findViewById<MaterialCardView>(R.id.cardInformes)       // <-- Cambiado de cardReportes
        val cardComprobantes = findViewById<MaterialCardView>(R.id.cardComprobantes) // <-- Cambiado de cardConfig
        val cardHistorial = findViewById<MaterialCardView>(R.id.cardHistorial)     // <-- Cambiado de cardSoporte

        // 2. Cargar nombre del admin desde Preferencias
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        val adminNombre = prefs.getString("admin_nombre", "Administrador") ?: "Administrador"

        // Actualizar el TextView
        tvAdminName.text = "Hola, $adminNombre"

        // 3. Configurar acciones (Click Listeners)

        cardDashboard.setOnClickListener {
            Toast.makeText(this, "Ir al Dashboard", Toast.LENGTH_SHORT).show()
            // Aquí navegarás: startActivity(Intent(this, DashboardActivity::class.java))
        }

        cardTecnicos.setOnClickListener {
            Toast.makeText(this, "Ir a Gestión de Técnicos", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, TecnicosActivity::class.java))
        }

        cardOrdenes.setOnClickListener {
            Toast.makeText(this, "Ir a Órdenes de Trabajo", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, OrdenesActivity::class.java))
        }

        cardInformes.setOnClickListener {
            Toast.makeText(this, "Ir a Informes", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, InformesActivity::class.java))
        }

        cardComprobantes.setOnClickListener {
            Toast.makeText(this, "Ir a Comprobantes", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ComprobantesActivity::class.java))
        }

        cardHistorial.setOnClickListener {
            Toast.makeText(this, "Ir al Historial", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, HistorialActivity::class.java))
        }

        // Botón de Cerrar Sesión
        btnLogout.setOnClickListener {
            // 1. Borrar datos guardados (sesión)
            prefs.edit().clear().apply()

            // 2. Volver al Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}