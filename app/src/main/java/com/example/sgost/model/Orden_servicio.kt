package com.example.sgost.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Orden_servicio(
    @SerializedName("ID_ORDEN_SERVICIO") val idOrden_servicio: Int? = null,
    @SerializedName("ID_CLIENTES") val idClientes: Int? = null,
    @SerializedName("ID_ADMINISTRADOR") val idAdministrador: Int? = null,
    @SerializedName("ID_TECNICOS") val idTecnicos: Int? = null,
    @SerializedName("ID_MOTOS") val idMotos: Int? = null,
    @SerializedName("Fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("Fecha_estimada") val fechaEstimada: String? = null,
    @SerializedName("Fecha_fin") val fechaFin: String? = null,
    @SerializedName("Estado") val estado: String? = null
) : Parcelable