package com.example.sgost

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.model.CarritoItem
import com.example.sgost.CartManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class CatalogoAdapter(
    private val onAddClick: (CarritoItem, Int) -> Unit
) : ListAdapter<CarritoItem, CatalogoAdapter.CatalogoViewHolder>(DiffCallback()) {

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_catalogo, parent, false)
        return CatalogoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogoViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun refreshCartQuantities() {
        submitList(currentList)
    }

    // ✅ Esta clase debe estar DENTRO de CatalogoAdapter
    inner class CatalogoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        private val chipTipo: Chip = itemView.findViewById(R.id.chipTipo)
        private val btnAgregar: MaterialButton = itemView.findViewById(R.id.btnAgregar)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidadCarrito)

        fun bind(item: CarritoItem, pos: Int) {
            tvNombre.text = item.nombre
            tvPrecio.text = formatoMoneda.format(item.precioUnitario)

            // ✅ Asignación de color corregida
            val colorHex = if (item.tipo == "SERVICIO") "#333333" else "#222222"
            chipTipo.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(colorHex))
            chipTipo.text = item.tipo

            // ✅ Lógica de carrito corregida
            val enCarrito = CartManager.getItems().find { it.id == item.id && it.tipo == item.tipo }
            val cantidad = enCarrito?.cantidad ?: 0

            if (cantidad > 0) {
                tvCantidad.text = "En carrito: $cantidad"
                tvCantidad.visibility = View.VISIBLE
                btnAgregar.text = "AGREGAR OTRO"
            } else {
                tvCantidad.visibility = View.GONE
                btnAgregar.text = "AGREGAR"
            }

            btnAgregar.setOnClickListener { onAddClick(item, pos) }
        }
    }

    // ✅ La clase DiffCallback debe definirse AQUÍ, antes de cerrar el corchete de la clase padre
    class DiffCallback : DiffUtil.ItemCallback<CarritoItem>() {
        override fun areItemsTheSame(oldItem: CarritoItem, newItem: CarritoItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CarritoItem, newItem: CarritoItem) = oldItem == newItem
    }
}