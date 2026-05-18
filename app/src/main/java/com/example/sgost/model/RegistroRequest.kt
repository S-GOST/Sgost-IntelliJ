package com.example.sgost.model

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val contrasena: String,
    val confirmar_contrasena: String
)