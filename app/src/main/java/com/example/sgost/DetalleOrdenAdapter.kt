package com.example.sgost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.R
import com.example.sgost.model.Detalles_orden_servicio
import java.text.NumberFormat
import java.util.Locale

class DetalleOrdenAdapter : ListAdapter<Detalles_orden_servicio, DetalleOrdenAdapter.ViewHolder>(DiffCallback()) {

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvGarantia: TextView = view.findViewById(R.id.tvGarantia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_orden, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detalle = getItem(position)

        // Determinar si es servicio o producto
        holder.tvTipo.text = if (detalle.idServicio != null) "🔧 Servicio" else "⚙️ Producto"
        holder.tvEstado.text = detalle.estado?.uppercase(Locale.getDefault()) ?: "PENDIENTE"
        holder.tvPrecio.text = detalle.precio?.let { formatoMoneda.format(it) } ?: "$0.00"
        holder.tvGarantia.text = detalle.garantia?.let { "${it} días" } ?: "Sin garantía"

        // Colorear estado
        when (detalle.estado?.lowercase(Locale.getDefault())) {
            "pendiente" -> holder.tvEstado.setTextColor(0xFFFF6600.toInt())
            "en proceso", "en_proceso" -> holder.tvEstado.setTextColor(0xFF2196F3.toInt())
            "finalizada", "completada" -> holder.tvEstado.setTextColor(0xFF4CAF50.toInt())
            else -> holder.tvEstado.setTextColor(0xFF888888.toInt())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(oldItem: Detalles_orden_servicio, newItem: Detalles_orden_servicio) =
            oldItem.idDetalle == newItem.idDetalle
        override fun areContentsTheSame(oldItem: Detalles_orden_servicio, newItem: Detalles_orden_servicio) =
            oldItem == newItem
    }
}