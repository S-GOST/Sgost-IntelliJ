package com.example.sgost // ✅ ESTO DEBE COINCIDIR CON LA CARPETA DONDE ESTÁ EL ARCHIVO

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.R
import com.example.sgost.model.Tecnico

class TecnicoAdapter(
    private val onEdit: (Tecnico) -> Unit,
    private val onDelete: (Tecnico) -> Unit
) : ListAdapter<Tecnico, TecnicoAdapter.TecnicoViewHolder>(TecnicoDiffCallback()) {

    class TecnicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvTecNombre)
        val tvUsuario: TextView = view.findViewById(R.id.tvTecDocumento)
        val tvExtra: TextView = view.findViewById(R.id.tvTecEstado)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TecnicoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tecnico, parent, false)
        return TecnicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TecnicoViewHolder, position: Int) {
        val tecnico = getItem(position)
        holder.tvNombre.text = tecnico.nombre ?: ""
        holder.tvUsuario.text = "👤 ${tecnico.usuario ?: ""}"
        holder.tvExtra.text = "📧 ${tecnico.correo ?: ""}"

        holder.btnEditar.setOnClickListener { onEdit(tecnico) }
        holder.btnEliminar.setOnClickListener { onDelete(tecnico) }
    }

    class TecnicoDiffCallback : DiffUtil.ItemCallback<Tecnico>() {
        override fun areItemsTheSame(oldItem: Tecnico, newItem: Tecnico) = oldItem.idTecnicos == newItem.idTecnicos
        override fun areContentsTheSame(oldItem: Tecnico, newItem: Tecnico) = oldItem == newItem
    }
}