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
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvProducto: TextView = view.findViewById(R.id.tvProducto)
        val ivProductoIcon: ImageView = view.findViewById(R.id.ivProductoIcon)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvGarantia: TextView = view.findViewById(R.id.tvGarantia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_orden, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detalle = getItem(position)
        Log.d("ADAPTER", "📦 onBind: Servicio=${detalle.nombreServicio} | Producto=${detalle.nombreProducto}")

        // 1. Servicio principal
        holder.tvTipo.text = detalle.nombreServicio ?: "Sin servicio"

        // 2. Producto e Imagen
        val nombreProd = detalle.nombreProducto ?: ""
        if (nombreProd.isNotBlank()) {
            holder.tvProducto.text = nombreProd
            holder.tvProducto.visibility = View.VISIBLE
            holder.ivProductoIcon.setImageResource(getIconForProduct(nombreProd))
        } else {
            holder.tvProducto.visibility = View.GONE
            holder.ivProductoIcon.setImageResource(R.mipmap.readi)
        }

        // 3. Precio
        holder.tvPrecio.text = if (detalle.precio != null && detalle.precio > 0) {
            formatoMoneda.format(detalle.precio)
        } else {
            "$0"
        }

        // 4. Garantía
        holder.tvGarantia.text = if (detalle.garantia != null && detalle.garantia > 0) {
            "${detalle.garantia} días"
        } else {
            "Sin garantía"
        }
    }

    private fun getIconForProduct(productName: String?): Int {
        val nombre = productName?.lowercase(Locale.getDefault()) ?: ""
        return when {
            nombre.contains("cadena") -> R.mipmap.cadena
            nombre.contains("filtro") -> R.mipmap.filtro
            nombre.contains("aceite") -> R.mipmap.motorex
            nombre.contains("pastilla") -> R.mipmap.pastillas
            else -> R.mipmap.readi
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            // ✅ CORRECCIÓN: Permite comparar null == null sin fallar
            old.idDetalleOrden == new.idDetalleOrden

        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old == new
    }
}