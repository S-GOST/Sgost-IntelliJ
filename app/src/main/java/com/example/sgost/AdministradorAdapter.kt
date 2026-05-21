package com.example.sgost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.example.sgost.R
import com.example.sgost.model.Administrador

class AdministradorAdapter(
    private val onEdit: (Administrador) -> Unit,
    private val onDelete: (Administrador) -> Unit
) : ListAdapter<Administrador, AdministradorAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvUsuario: TextView = view.findViewById(R.id.tvUsuario)
        val btnEditar: View = view.findViewById(R.id.btnEditar)
        val btnEliminar: View = view.findViewById(R.id.btnEliminar)
        val cardRoot: MaterialCardView = view.findViewById(R.id.cardItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_administrador, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val admin = getItem(position)

        holder.tvNombre.text = admin.nombre ?: "Sin nombre"
        holder.tvUsuario.text = admin.usuario ?: "Sin usuario"

        holder.btnEditar.setOnClickListener { onEdit(admin) }
        holder.btnEliminar.setOnClickListener { onDelete(admin) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Administrador>() {
        override fun areItemsTheSame(oldItem: Administrador, newItem: Administrador) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Administrador, newItem: Administrador) = oldItem == newItem
    }
}