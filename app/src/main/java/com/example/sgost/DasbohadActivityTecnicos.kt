package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivityTecnicos : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_tecnicos)

        // 1. Referenciar vistas
        val tvTecnicoName = findViewById<TextView>(R.id.tvTecnicoName)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Tarjetas del Menú
        val cardMisOrdenes = findViewById<MaterialCardView>(R.id.cardMisOrdenes)
        val cardActualizarEstado = findViewById<MaterialCardView>(R.id.cardActualizarEstado)
        val cardHistorial = findViewById<MaterialCardView>(R.id.cardHistorial)
        val cardPerfil = findViewById<MaterialCardView>(R.id.cardPerfil)
        val cardNotificaciones = findViewById<MaterialCardView>(R.id.cardNotificaciones)
        val cardAyuda = findViewById<MaterialCardView>(R.id.cardAyuda)

        // 2. Cargar nombre del técnico desde Preferencias
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        val tecnicoNombre = prefs.getString("tecnico_nombre", "Técnico") ?: "Técnico"

        // Actualizar el TextView
        tvTecnicoName.text = "Hola, $tecnicoNombre"

        // 3. Configurar acciones (Click Listeners)

        cardMisOrdenes.setOnClickListener {
            Toast.makeText(this, "Ir a Mis Órdenes de Trabajo", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, MisOrdenesActivity::class.java))
        }

        cardActualizarEstado.setOnClickListener {
            Toast.makeText(this, "Actualizar Estado de Orden", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, ActualizarEstadoActivity::class.java))
        }

        cardHistorial.setOnClickListener {
            Toast.makeText(this, "Ver Historial de Trabajo", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, HistorialTecnicoActivity::class.java))
        }

        cardPerfil.setOnClickListener {
            Toast.makeText(this, "Ver mi Perfil", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, PerfilTecnicoActivity::class.java))
        }

        cardNotificaciones.setOnClickListener {
            Toast.makeText(this, "Ver Notificaciones", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        cardAyuda.setOnClickListener {
            Toast.makeText(this, "Centro de Ayuda", Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, AyudaActivity::class.java))
        }

        // Botón de Cerrar Sesión
        btnLogout.setOnClickListener {
            // 1. Borrar datos guardados (sesión)
            prefs.edit().clear().apply()

            // 2. Volver al Login de Técnicos
            val intent = Intent(this, LoginActivityTecnicos::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}