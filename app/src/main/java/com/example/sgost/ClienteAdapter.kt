package com.example.sgost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.R
import com.example.sgost.model.Cliente

class ClienteAdapter(
    private val onEdit: (Cliente) -> Unit,
    private val onDelete: (Cliente) -> Unit
) : ListAdapter<Cliente, ClienteAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvCliNombre)
        val tvCorreo: TextView = view.findViewById(R.id.tvCliCorreo)
        val tvTelefono: TextView = view.findViewById(R.id.tvCliTelefono)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = getItem(position)
        holder.tvNombre.text = cliente.nombre ?: ""
        holder.tvCorreo.text = "📧 ${cliente.correo ?: ""}"
        holder.tvTelefono.text = "📞 ${cliente.telefono ?: ""}"

        holder.btnEditar.setOnClickListener { onEdit(cliente) }
        holder.btnEliminar.setOnClickListener { onDelete(cliente) }
    }

    class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente) = oldItem == newItem
    }
}