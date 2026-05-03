package org.sgost.project.domain.usecase

import org.sgost.project.domain.model.User
import org.sgost.project.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa usuario y contrasena"))
        }

        return authRepository.login(username, password)
    }
}
