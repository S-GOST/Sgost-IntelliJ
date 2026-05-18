package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView

class DashboardActivityTecnicos : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_tecnicos)

        // 📌 Referencias UI
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenidaTec)
        val tvNombre = findViewById<TextView>(R.id.tvNombreTec)
        val tvEmail = findViewById<TextView>(R.id.tvEmailTec)
        val tvTelefono = findViewById<TextView>(R.id.tvTelefonoTec)
        val tvUbicacion = findViewById<TextView>(R.id.tvUbicacionTec)

        val cardOrdenes = findViewById<MaterialCardView>(R.id.cardMisOrdenes)
        val cardHistorial = findViewById<MaterialCardView>(R.id.cardHistorial)
        val cardPerfil = findViewById<MaterialCardView>(R.id.cardPerfil)
        val btnLogout = findViewById<Button>(R.id.btnLogoutTec)

        // 📊 Cargar datos reales de sesión
        val nombre = prefs.getString("user_nombre", "Técnico") ?: "Técnico"
        val email = prefs.getString("user_correo", "No registrado") ?: "No registrado"
        val telefono = prefs.getString("user_telefono", "No registrado") ?: "No registrado"
        val ubicacion = prefs.getString("user_ubicacion", "No registrada") ?: "No registrada"

        tvBienvenida.text = "Hola, $nombre"
        tvNombre.text = nombre
        tvEmail.text = email
        tvTelefono.text = telefono
        tvUbicacion.text = ubicacion

        // 🚀 Navegación activa
        //cardOrdenes.setOnClickListener {
        //  startActivity(Intent(this, MisOrdenesActivity::class.java))
        //}
        //cardHistorial.setOnClickListener {
        //    startActivity(Intent(this, HistorialTecnicoActivity::class.java))
        //}
        //cardPerfil.setOnClickListener {
        //  startActivity(Intent(this, PerfilTecnicoActivity::class.java))
        //}

        // 🚪 Cerrar sesión
        btnLogout.setOnClickListener {
            prefs.edit { clear() }
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}