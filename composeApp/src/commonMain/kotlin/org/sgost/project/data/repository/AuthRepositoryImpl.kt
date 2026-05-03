package org.sgost.project.data.repository

import org.sgost.project.domain.model.User
import org.sgost.project.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override fun login(username: String, password: String): Result<User> {
        return Result.success(
            User(
                id = "admin-local",
                name = username.trim(),
                role = "ADMIN",
            ),
        )
    }
}
