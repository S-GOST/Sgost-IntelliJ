package com.example.sgost.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvNombreItem: TextView = view.findViewById(R.id.tvNombreItem)
        val tvGarantia: TextView = view.findViewById(R.id.tvGarantia)
        val tvPrecioUnitario: TextView = view.findViewById(R.id.tvPrecioUnitario)
        val tvTotalPrice: TextView = view.findViewById(R.id.tvTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_orden, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detalle = getItem(position)

        // 1. Determinar si tiene servicio o producto (o ambos)
        val tieneServicio = !detalle.nombreServicio.isNullOrEmpty()
        val tieneProducto = !detalle.nombreProducto.isNullOrEmpty()

        // ✅ LÓGICA PARA MOSTRAR AMBOS NOMBRES EN UNA SOLA LÍNEA
        val nombreFinal = when {
            tieneServicio && tieneProducto -> "${detalle.nombreServicio} + ${detalle.nombreProducto}"
            tieneServicio -> detalle.nombreServicio!!
            tieneProducto -> detalle.nombreProducto!!
            else -> "Detalle sin nombre"
        }

        holder.tvNombreItem.text = nombreFinal
        holder.ivIcon.setImageResource(
            if (tieneServicio) R.mipmap.readi else
                if (tieneProducto) R.mipmap.readi else
                    R.mipmap.readi // Asegúrate de tener un icono genérico o usa el mismo
        )

        // 2. Garantía
        val garantia = detalle.garantia ?: 0
        holder.tvGarantia.text = if (garantia > 0) "$garantia días de garantía" else "Sin garantía"

        // 3. Precio (Ya viene sumado desde la Activity)
        val precio = detalle.precio ?: 0.0
        val precioFormateado = formatoMoneda.format(precio)
        holder.tvTotalPrice.text = precioFormateado

        if (tieneServicio && tieneProducto) {
            holder.tvPrecioUnitario.text = "Precio total combinado"
        } else {
            holder.tvPrecioUnitario.text = "$precioFormateado / unidad"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio): Boolean {
            // Usar clave compuesta: id + tipo (servicio o producto)
            // Esto permite que un servicio y un producto del mismo detalle se muestren como filas separadas
            val oldEsServicio = old.idServicios != null && old.idServicios > 0
            val newEsServicio = new.idServicios != null && new.idServicios > 0
            return old.idDetalleOrden == new.idDetalleOrden && oldEsServicio == newEsServicio
        }
        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old == new
    }
}