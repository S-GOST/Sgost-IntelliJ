package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView

class DashboardActivityClientes : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_clientes)

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val tvNombre = findViewById<TextView>(R.id.tvNombreCliente)
        val tvEmail = findViewById<TextView>(R.id.tvEmailCliente)
        val tvTelefono = findViewById<TextView>(R.id.tvTelefonoCliente)
        val tvUbicacion = findViewById<TextView>(R.id.tvUbicacionCliente)

        val cardOrdenes = findViewById<MaterialCardView>(R.id.cardVerOrdenes)
        val cardCarrito = findViewById<MaterialCardView>(R.id.cardCarrito)
        val cardNotif = findViewById<MaterialCardView>(R.id.cardNotificaciones)
        val cardAyuda = findViewById<MaterialCardView>(R.id.cardAyuda)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 📊 Leer directamente de SharedPreferences
        val nombre = prefs.getString("user_nombre", "Cliente") ?: "Cliente"
        val email = prefs.getString("user_correo", "No registrado") ?: "No registrado"
        val telefono = prefs.getString("user_telefono", "No registrado") ?: "No registrado"
        val ubicacion = prefs.getString("user_ubicacion", "No registrada") ?: "No registrada"

        tvBienvenida.text = "Hola, $nombre"
        tvNombre.text = nombre
        tvEmail.text = email
        tvTelefono.text = telefono
        tvUbicacion.text = ubicacion

        cardOrdenes.setOnClickListener { startActivity(Intent(this, OrdenServicioActivity::class.java)) }
        cardCarrito.setOnClickListener { startActivity(Intent(this, CarritoActivity::class.java)) }
        //cardNotif.setOnClickListener { startActivity(Intent(this, NotificacionesActivity::class.java)) }
        //cardAyuda.setOnClickListener { startActivity(Intent(this, AyudaActivity::class.java)) }

        btnLogout.setOnClickListener {
            prefs.edit { clear() }
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}