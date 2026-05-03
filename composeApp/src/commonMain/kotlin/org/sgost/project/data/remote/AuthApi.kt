package org.sgost.project.data.remote

interface AuthApi {
    fun login(request: LoginRequestDto): LoginResponseDto
}
