package org.sgost.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.sgost.project.presentation.navigation.AppRoute
import org.sgost.project.presentation.screens.AdminPanelScreen
import org.sgost.project.presentation.screens.LoginScreen
import org.sgost.project.presentation.screens.ServiceFormScreen
import org.sgost.project.presentation.theme.SgostTheme

@Composable
@Preview
fun App() {
    SgostTheme {
        var route by remember { mutableStateOf<AppRoute>(AppRoute.Login) }

        when (route) {
            AppRoute.Login -> LoginScreen(
                onLoginSuccess = { route = AppRoute.AdminPanel },
            )

            AppRoute.AdminPanel -> AdminPanelScreen(
                onOpenForm = { route = AppRoute.ServiceForm },
                onLogout = { route = AppRoute.Login },
            )

            AppRoute.ServiceForm -> ServiceFormScreen(
                onBack = { route = AppRoute.AdminPanel },
            )
        }
    }
}
