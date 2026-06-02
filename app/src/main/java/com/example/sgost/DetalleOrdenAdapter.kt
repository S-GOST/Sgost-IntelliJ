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

        Log.d("ADAPTER", "📦 onBind: Servicio=[${detalle.idProductos}] | Producto=[${detalle.idProductos}]")

        // 1. Servicio principal
        holder.tvTipo.text = detalle.idServicios ?: "Sin servicio"

        // 2. Producto asociado e Imagen
        if (!detalle.idProductos.isNullOrEmpty()) {
            holder.tvProducto.text = detalle.idProductos
            holder.tvProducto.visibility = View.VISIBLE
            // Asignar la imagen de MIPMAP según el nombre
            holder.ivProductoIcon.setImageResource(getIconForProduct(detalle.idProductos))
        } else {
            holder.tvProducto.visibility = View.GONE
            // Icono genérico si no hay producto
            holder.ivProductoIcon.setImageResource(R.mipmap.readi)
        }

        // 3. Precio y Garantía
        holder.tvPrecio.text = formatoMoneda.format(detalle.precio)
        holder.tvGarantia.text = if (detalle.garantia > 0) "${detalle.garantia} días" else "Sin garantía"
    }

    /**
     * 🔧 Busca el icono en la carpeta mipmap.
     * IMPORTANTE: Asegúrate de que los archivos .png existan en res/mipmap-*/
    private fun getIconForProduct(productName: String?): Int {
        val nombre = productName?.lowercase(Locale.getDefault()) ?: ""

        return when {
            nombre.contains("cadena") -> R.mipmap.cadena      // Existe en tu captura
            nombre.contains("filtro") -> R.mipmap.filtro      // Existe en tu captura

            // Los siguientes DEBEN ser creados en la carpeta mipmap o la app fallará:
            nombre.contains("aceite") || nombre.contains("motorex") -> R.mipmap.motorex
            nombre.contains("pastilla") || nombre.contains("freno") -> R.mipmap.pastillas
            else -> R.mipmap.readi // Fallback (Icono genérico)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old.idDetalleOrden == new.idDetalleOrden
        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio) =
            old == new
    }
}