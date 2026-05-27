package com.example.sgost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.R
import com.example.sgost.model.Orden_servicio
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale

class OrdenServicioAdapter(
    private val onOrderClick: (Orden_servicio) -> Unit
) : ListAdapter<Orden_servicio, OrdenServicioAdapter.ViewHolder>(DiffCallback()) {

    // Formatos para BD y UI
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val uiFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardRoot: MaterialCardView = view.findViewById(R.id.cardOrden)
        val tvIdOrden: TextView = view.findViewById(R.id.tvIdOrden)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orden_servicio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = getItem(position)

        // 📝 Datos básicos
        holder.tvIdOrden.text = "Orden #${orden.idOrden_servicio ?: "N/A"}"
        holder.tvCliente.text = "Cliente ID: ${orden.idClientes ?: "N/A"}"

        // 📅 Formateo seguro de fecha
        holder.tvFecha.text = formatearFecha(orden.fechaInicio)

        // 🎨 Código de colores por estado
        val estado = orden.estado?.lowercase(Locale.getDefault())
        holder.tvEstado.text = orden.estado?.uppercase(Locale.getDefault()) ?: "PENDIENTE"
        when (estado) {
            "pendiente" -> holder.tvEstado.setTextColor(0xFFFF6600.toInt()) // Naranja
            "en_proceso", "en proceso" -> holder.tvEstado.setTextColor(0xFF2196F3.toInt()) // Azul
            "completada", "terminada", "finalizada" -> holder.tvEstado.setTextColor(0xFF4CAF50.toInt()) // Verde
            "cancelada", "rechazada" -> holder.tvEstado.setTextColor(0xFFE53935.toInt()) // Rojo
            else -> holder.tvEstado.setTextColor(0xFF888888.toInt()) // Gris
        }

        // 👆 Click listener
        holder.cardRoot.setOnClickListener {
            onOrderClick(orden)
        }
    }

    private fun formatearFecha(fechaDb: String?): String {
        if (fechaDb.isNullOrEmpty()) return "Sin fecha"
        return try {
            val date = dbFormat.parse(fechaDb)
            date?.let { uiFormat.format(it) } ?: fechaDb
        } catch (e: Exception) {
            fechaDb // Retorna raw si falla el parseo
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Orden_servicio>() {
        override fun areItemsTheSame(oldItem: Orden_servicio, newItem: Orden_servicio) =
            oldItem.idOrden_servicio == newItem.idOrden_servicio
        override fun areContentsTheSame(oldItem: Orden_servicio, newItem: Orden_servicio) =
            oldItem == newItem
    }
}