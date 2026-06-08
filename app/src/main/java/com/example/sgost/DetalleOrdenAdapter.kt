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
        val ivTipoIcon: ImageView = view.findViewById(R.id.ivTipoIcon)
        val tvNombreItem: TextView = view.findViewById(R.id.tvNombreItem)
        val tvCategoriaItem: TextView = view.findViewById(R.id.tvCategoriaItem)
        val tvPrecioUnitario: TextView = view.findViewById(R.id.tvPrecioUnitario)
        val tvSubtotalItem: TextView = view.findViewById(R.id.tvSubtotalItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_orden, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detalle = getItem(position)

        // ✅ LOGGING: Verifica que llegan los números reales
        Log.d("ADAPTER", "📦 Pos:$position -> Precio:${detalle.precio} | Garantia:${detalle.garantia}")

        val nombreServ = detalle.nombreServicio?.takeIf { it.isNotBlank() }
        val nombreProd = detalle.nombreProducto?.takeIf { it.isNotBlank() }
        val esProducto = nombreProd != null || (detalle.idProductos != null && detalle.idProductos!! > 0)

        // 1. Icono
        holder.ivTipoIcon.setImageResource(getIconForResource(nombreServ, nombreProd, esProducto))

        // 2. Nombre principal
        holder.tvNombreItem.text = when {
            nombreServ != null -> nombreServ
            nombreProd != null -> nombreProd
            detalle.idServicios != null && detalle.idServicios!! > 0 -> "Servicio #${detalle.idServicios}"
            detalle.idProductos != null && detalle.idProductos!! > 0 -> "Producto #${detalle.idProductos}"
            else -> "Detalle sin asignar"
        }

        // 3. Garantía REAL (Int)
        val garantia = detalle.garantia ?: 0
        holder.tvCategoriaItem.text = if (garantia > 0) "$garantia días de garantía" else "Sin garantía"

        // 4. Precios REALES (Double)
        val precio = detalle.precio ?: 0.0
        val precioFormateado = if (precio > 0) formatoMoneda.format(precio) else "$0"
        holder.tvPrecioUnitario.text = "$precioFormateado / unidad"
        holder.tvSubtotalItem.text = precioFormateado
    }

    private fun getIconForResource(nombreServ: String?, nombreProd: String?, esProducto: Boolean): Int {
        if (!esProducto) return R.mipmap.readi
        val nombre = nombreProd?.lowercase(Locale.getDefault()) ?: ""
        return when {
            nombre.contains("cadena") -> R.mipmap.cadena
            nombre.contains("filtro") -> R.mipmap.filtro
            nombre.contains("aceite") -> R.mipmap.motorex
            nombre.contains("pastilla") -> R.mipmap.pastillas
            else -> R.mipmap.readi
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Detalles_orden_servicio>() {
        override fun areItemsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio): Boolean {
            return (old.idDetalleOrden != null && old.idDetalleOrden == new.idDetalleOrden) ||
                    (old.nombreServicio == new.nombreServicio && old.nombreProducto == new.nombreProducto)
        }
        override fun areContentsTheSame(old: Detalles_orden_servicio, new: Detalles_orden_servicio): Boolean {
            return old.nombreServicio == new.nombreServicio &&
                    old.nombreProducto == new.nombreProducto &&
                    old.precio == new.precio &&
                    old.garantia == new.garantia
        }
    }
}