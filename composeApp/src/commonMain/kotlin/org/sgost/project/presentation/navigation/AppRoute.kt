package org.sgost.project.presentation.navigation

sealed interface AppRoute {
    data object Login : AppRoute
    data object AdminPanel : AppRoute
    data object ServiceForm : AppRoute
}
