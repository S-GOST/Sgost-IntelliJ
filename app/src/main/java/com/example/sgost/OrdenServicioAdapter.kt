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
import java.text.SimpleDateFormat
import java.util.Locale

class OrdenServicioAdapter(
    private val onOrderClick: (Orden_servicio) -> Unit
) : ListAdapter<Orden_servicio, OrdenServicioAdapter.ViewHolder>(DiffCallback()) {

    private val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrdenId: TextView = view.findViewById(R.id.tvOrdenId)
        val tvIdMoto: TextView = view.findViewById(R.id.tvIdMoto)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvFechaInicio: TextView = view.findViewById(R.id.tvFechaInicio)
        val tvFechaEstimada: TextView = view.findViewById(R.id.tvFechaEstimada)
        val tvFechaFin: TextView = view.findViewById(R.id.tvFechaFin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orden_servicio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = getItem(position)

        // 🔵 CAMBIO: Mostrar nombre del cliente en lugar de "Orden #X"
        val clienteNombre = orden.nombreCliente ?: "Cliente sin asignar"
        holder.tvOrdenId.text = clienteNombre

        // Mantener info de la moto
        val motoTexto = if (orden.marcaMoto != null && orden.modeloMoto != null) {
            "🏍️ ${orden.marcaMoto} ${orden.modeloMoto} (${orden.placaMoto ?: "S/P"})"
        } else {
            "🏍️ Sin moto asignada"
        }
        holder.tvIdMoto.text = motoTexto

        // 2. Estado con colores dinámicos (tu código existente)
        val estado = orden.estado?.uppercase(Locale.getDefault()) ?: "PENDIENTE"
        holder.tvEstado.text = estado
        holder.tvEstado.setTextColor(
            when (estado.lowercase(Locale.getDefault())) {
                "finalizada", "completada" -> 0xFF4CAF50.toInt()
                "pendiente" -> 0xFFFF6600.toInt()
                "en proceso", "en_proceso" -> 0xFF2196F3.toInt()
                else -> 0xFF888888.toInt()
            }
        )

        // 3. Fechas formateadas (tu código existente)
        holder.tvFechaInicio.text = formatDate(orden.fechaInicio)
        holder.tvFechaEstimada.text = formatDate(orden.fechaEstimada)
        holder.tvFechaFin.text = formatDate(orden.fechaFin)

        // 4. Click listener
        holder.itemView.setOnClickListener {
            orden.idOrden_servicio?.let { onOrderClick(orden) }
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "N/A"
        return try {
            // Extrae solo YYYY-MM-DD para formatear a dd/MM/yyyy
            val cleanDate = dateStr.substring(0, 10)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cleanDate)
            date?.let { outputFormat.format(it) } ?: cleanDate
        } catch (e: Exception) {
            // Fallback: convierte "2025-11-05" a "05/11/2025" manualmente
            dateStr.take(10).split("-").let {
                "${it[2]}/${it[1]}/${it[0]}"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Orden_servicio>() {
        override fun areItemsTheSame(old: Orden_servicio, new: Orden_servicio) =
            old.idOrden_servicio == new.idOrden_servicio
        override fun areContentsTheSame(old: Orden_servicio, new: Orden_servicio) = old == new
    }
}