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

    // Formato moneda Colombia: $ 120.000
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

        // Log para depurar
        Log.d("ADAPTER", "🎨 Renderizando -> ${detalle.nombreServicio ?: detalle.nombreProducto} | Precio: ${detalle.precio} | Garantia: ${detalle.garantia}")

        // 1. Nombre
        val esServicio = !detalle.nombreServicio.isNullOrEmpty() || (detalle.idServicios != null && detalle.idServicios!! > 0)
        val nombre = if (esServicio) detalle.nombreServicio else detalle.nombreProducto
        holder.tvNombreItem.text = nombre ?: "Detalle sin nombre"
        holder.ivIcon.setImageResource(if (esServicio) R.mipmap.readi else R.mipmap.readi)

        // 2. Garantía
        val garantia = detalle.garantia ?: 0
        holder.tvGarantia.text = if (garantia > 0) "$garantia días de garantía" else "Sin garantía"

        // 3. Precios
        val precio = detalle.precio ?: 0.0
        val precioFormateado = formatoMoneda.format(precio)
        holder.tvTotalPrice.text = precioFormateado
        holder.tvPrecioUnitario.text = "$precioFormateado / unidad"
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) = old.idDetalleOrden == new.idDetalleOrden
        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) = old == new
    }
}