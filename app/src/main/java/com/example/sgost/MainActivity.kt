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

        // 1. Referenciar elementos del diseño (KTM Menu)
        val tvAdminName = findViewById<TextView>(R.id.tvAdminName)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Tarjetas del menú (usando la clase correcta de Material Design)
        val cardUsuarios = findViewById<MaterialCardView>(R.id.cardUsuarios)
        val cardDashboard = findViewById<MaterialCardView>(R.id.cardDashboard)

        // 2. Obtener el nombre del admin guardado en el Login
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        // Si no hay nombre, muestra "Administrador" por defecto
        val nombre = prefs.getString("admin_nombre", "Administrador")

        // 3. Mostrar el nombre en pantalla
        tvAdminName.text = "Hola, $nombre"

        // 4. Configurar Botón de Cerrar Sesión
        btnLogout.setOnClickListener {
            // Borrar sesión
            prefs.edit().clear().apply()

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            // Volver al Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Importante: cierra MainActivity para que el botón ATRÁS no regrese
        }

        // 5. Configurar clics de las tarjetas (Ejemplos)
        cardDashboard?.setOnClickListener {
            Toast.makeText(this, "Abriendo Dashboard...", Toast.LENGTH_SHORT).show()
        }

        cardUsuarios?.setOnClickListener {
            Toast.makeText(this, "Gestión de Usuarios...", Toast.LENGTH_SHORT).show()
        }
    }
}