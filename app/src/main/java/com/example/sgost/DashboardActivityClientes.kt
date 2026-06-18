package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DashboardActivityClientes : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("sgost_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_clientes)

        // ==========================================
        // 1. INICIALIZACIÓN DE VISTAS (IDs exactos del XML)
        // ==========================================
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val tvNombreCliente = findViewById<TextView>(R.id.tvNombreCliente)
        val tvEmailCliente = findViewById<TextView>(R.id.tvEmailCliente)
        val tvTelefonoCliente = findViewById<TextView>(R.id.tvTelefonoCliente)
        val tvUbicacionCliente = findViewById<TextView>(R.id.tvUbicacionCliente)

        val cardVerOrdenes = findViewById<MaterialCardView>(R.id.cardVerOrdenes)
        val cardCarrito = findViewById<MaterialCardView>(R.id.cardCarrito)
        val cardNotificaciones = findViewById<MaterialCardView>(R.id.cardNotificaciones)
        val cardAyuda = findViewById<MaterialCardView>(R.id.cardAyuda)
        val cardComprobantes = findViewById<MaterialCardView>(R.id.cardComprobantes)
        val cardHistorialCompras = findViewById<MaterialCardView>(R.id.cardHistorialCompras)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val btnIrACatalogo = findViewById<MaterialButton>(R.id.btnIrACatalogo) // ✅ INICIALIZACIÓN FALTANTE

        // ==========================================
        // 2. CARGAR DATOS DEL USUARIO
        // ==========================================
        val nombre = prefs.getString("user_nombre", "Cliente") ?: "Cliente"
        val email = prefs.getString("user_correo", "No registrado") ?: "No registrado"
        val telefono = prefs.getString("user_telefono", "No registrado") ?: "No registrado"
        val ubicacion = prefs.getString("user_ubicacion", "No registrada") ?: "No registrada"

        // ==========================================
        // 3. ACTUALIZAR UI
        // ==========================================
        tvBienvenida.text = "Hola, $nombre"
        tvNombreCliente.text = "$nombre • En sesión"
        tvEmailCliente.text = "📧 $email"
        tvTelefonoCliente.text = "📱 $telefono"
        tvUbicacionCliente.text = "📍 $ubicacion"

        // ==========================================
        // 4. CONFIGURAR CLICK LISTENERS
        // ==========================================
        cardVerOrdenes.setOnClickListener {
            startActivity(Intent(this, OrdenServicioActivity::class.java))
        }

        cardCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        btnIrACatalogo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Cards adicionales (descomenta cuando las implementes)
        // cardNotificaciones.setOnClickListener { startActivity(Intent(this, NotificacionesActivity::class.java)) }
        // cardAyuda.setOnClickListener { startActivity(Intent(this, AyudaActivity::class.java)) }
        // cardComprobantes.setOnClickListener { startActivity(Intent(this, ComprobantesActivity::class.java)) }
        // cardHistorialCompras.setOnClickListener { startActivity(Intent(this, HistorialActivity::class.java)) }

        btnLogout.setOnClickListener {
            // Limpia toda la sesión
            prefs.edit { clear() }

            // Redirige al Login y limpia la pila de actividades
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}