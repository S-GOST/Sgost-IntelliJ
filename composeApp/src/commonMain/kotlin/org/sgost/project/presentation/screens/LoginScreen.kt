package org.sgost.project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sgost.project.data.repository.AuthRepositoryImpl
import org.sgost.project.domain.usecase.LoginUseCase
import org.sgost.project.presentation.components.SgostPrimaryButton
import org.sgost.project.presentation.components.SgostTextField
import org.sgost.project.presentation.theme.KtmOrange
import org.sgost.project.presentation.theme.RacingBlack

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    val loginUseCase = remember { LoginUseCase(AuthRepositoryImpl()) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RacingBlack, RacingBlack, KtmOrange.copy(alpha = 0.55f)),
                ),
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column {
                Text(
                    text = "SGOST",
                    color = KtmOrange,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Admin racing access",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
            }

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Iniciar sesion",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    SgostTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            error = null
                        },
                        label = "Usuario",
                    )
                    SgostTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = "Contrasena",
                        isPassword = true,
                    )
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SgostPrimaryButton(
                        text = "ENTRAR AL PANEL",
                        onClick = {
                            val result = loginUseCase(username, password)
                            if (result.isSuccess) {
                                onLoginSuccess()
                            } else {
                                error = result.exceptionOrNull()?.message ?: "No se pudo iniciar sesion"
                            }
                        },
                    )
                }
            }
        }
    }
}
