package com.example.sgost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.R
import com.example.sgost.model.CarritoItem
import java.text.NumberFormat
import java.util.Locale

class CarritoAdapter(
    private val lista: List<CarritoItem>,
    private val onEliminar: (Int) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreItem)
        val tvTipo: TextView = view.findViewById(R.id.etTipoDoc)  // o tvCategoriaItem, según tu layout
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioUnitario)
        val tvCantidad: TextView = view.findViewById(R.id.tvPrecio)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // Usamos los campos reales de CarritoItem
        holder.tvNombre.text = item.nombre
        holder.tvTipo.text = item.tipo  // "Producto" o "Servicio"
        holder.tvPrecio.text = "${formatoMoneda.format(item.precioUnitario)} / unidad"
        holder.tvCantidad.text = "Cant: ${item.cantidad}"
        holder.tvSubtotal.text = formatoMoneda.format(item.subtotal)

        holder.btnEliminar.setOnClickListener {
            onEliminar(position)
        }
    }

    override fun getItemCount(): Int = lista.size
}