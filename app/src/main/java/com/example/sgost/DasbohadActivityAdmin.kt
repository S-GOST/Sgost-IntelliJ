package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView

class DasbohadActivityAdmin : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Validar sesión activa y rol
        val token = prefs.getString("auth_token", null)
        val userType = prefs.getString("user_type", "")

        if (token == null || userType != "admin") {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_dashboard_admin)

        // 📌 Referencias UI
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenidaAdmin)
        val tvNombre = findViewById<TextView>(R.id.tvNombreAdmin)
        val tvEmail = findViewById<TextView>(R.id.tvEmailAdmin)
        val tvTelefono = findViewById<TextView>(R.id.tvTelefonoAdmin)
        val tvUbicacion = findViewById<TextView>(R.id.tvUbicacionAdmin)

        // ✅ AGREGADO: Referencia a la tarjeta de Clientes
        val cardClientes = findViewById<MaterialCardView>(R.id.cardClientes)
        val cardTecnicos = findViewById<MaterialCardView>(R.id.cardTecnicos)
        val cardOrdenes = findViewById<MaterialCardView>(R.id.cardOrdenes)
        val cardInformes = findViewById<MaterialCardView>(R.id.cardInformes)
        val cardComprobantes = findViewById<MaterialCardView>(R.id.cardComprobantes)
        val cardHistorial = findViewById<MaterialCardView>(R.id.cardHistorial)
        val btnLogout = findViewById<Button>(R.id.btnLogoutAdmin)

        // 📊 Cargar datos reales guardados en sesión
        val nombre = prefs.getString("user_nombre", "Admin") ?: "Admin"
        val email = prefs.getString("user_correo", "No registrado") ?: "No registrado"
        val telefono = prefs.getString("user_telefono", "No registrado") ?: "No registrado"
        val ubicacion = prefs.getString("user_ubicacion", "No registrada") ?: "No registrada"

        tvBienvenida.text = "Hola, $nombre"
        tvNombre.text = nombre
        tvEmail.text = email
        tvTelefono.text = telefono
        tvUbicacion.text = ubicacion

        // ✅ REDIRECCIÓN A GESTIÓN DE CLIENTES
        cardClientes.setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }

        // ✅ REDIRECCIÓN A GESTIÓN DE TÉCNICOS
        cardTecnicos.setOnClickListener {
            startActivity(Intent(this, TecnicosActivity::class.java))
        }

        cardOrdenes.setOnClickListener { /* startActivity(Intent(this, OrdenesActivity::class.java)) */ }
        cardInformes.setOnClickListener { /* startActivity(Intent(this, InformesActivity::class.java)) */ }
        cardComprobantes.setOnClickListener { /* startActivity(Intent(this, ComprobantesActivity::class.java)) */ }
        cardHistorial.setOnClickListener { /* startActivity(Intent(this, HistorialGlobalActivity::class.java)) */ }

        // 🚪 Cerrar sesión
        btnLogout.setOnClickListener {
            prefs.edit { clear() }
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}