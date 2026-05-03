package org.sgost.project.data.remote

data class LoginResponseDto(
    val token: String,
    val name: String,
    val role: String,
)
