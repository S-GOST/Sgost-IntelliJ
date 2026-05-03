package org.sgost.project.domain.repository

import org.sgost.project.domain.model.User

interface AuthRepository {
    fun login(username: String, password: String): Result<User>
}
