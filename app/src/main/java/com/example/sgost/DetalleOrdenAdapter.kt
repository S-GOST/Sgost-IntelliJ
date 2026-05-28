package com.example.sgost.adapter

import android.util.Log
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

class DetalleOrdenAdapter : ListAdapter<_root_ide_package_.com.example.sgost.model.Detalles_orden_servicio, DetalleOrdenAdapter.ViewHolder>(DiffCallback()) {

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

        // 🔍 LOG DE DEBUG (Quítalo cuando funcione)
        Log.d("ADAPTER", "📦 onBind: ${detalle.nombreServicio} / ${detalle.nombreProducto} | Precio: ${detalle.precio}")

        // Determinar qué mostrar
        val esServicio = detalle.nombreServicio?.isNotBlank() == true
        val nombre = if (esServicio) detalle.nombreServicio!! else detalle.nombreProducto ?: "Sin nombre"
        val icono = if (esServicio) "🔧" else "⚙️"

        holder.tvTipo.text = "$icono $nombre"
        holder.tvEstado.text = detalle.estado?.uppercase(Locale.getDefault()) ?: "PENDIENTE"
        holder.tvPrecio.text = formatoMoneda.format(detalle.precio)
        holder.tvGarantia.text = if (detalle.garantia > 0) "${detalle.garantia} días" else "Sin garantía"

        // Colorear estado
        when (detalle.estado?.lowercase(Locale.getDefault())) {
            "pendiente" -> holder.tvEstado.setTextColor(0xFFFF6600.toInt())
            "en proceso", "en_proceso" -> holder.tvEstado.setTextColor(0xFF2196F3.toInt())
            "finalizada", "completada" -> holder.tvEstado.setTextColor(0xFF4CAF50.toInt())
            else -> holder.tvEstado.setTextColor(0xFF888888.toInt())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<_root_ide_package_.com.example.sgost.model.Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old.idDetalle == new.idDetalle
        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old == new
    }
}