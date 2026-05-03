package org.sgost.project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sgost.project.presentation.components.SgostPrimaryButton
import org.sgost.project.presentation.components.SgostSecondaryButton
import org.sgost.project.presentation.theme.KtmOrange

@Composable
fun AdminPanelScreen(
    onOpenForm: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Panel admin",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Sgost operaciones",
                    color = KtmOrange,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminMetricCard(title = "Ordenes", value = "12", modifier = Modifier.weight(1f))
            AdminMetricCard(title = "Activas", value = "4", modifier = Modifier.weight(1f))
        }

        AdminMetricCard(
            title = "Estado",
            value = "Listo para registrar servicios",
            modifier = Modifier.fillMaxWidth(),
        )

        SgostPrimaryButton(
            text = "ABRIR FORMULARIO",
            onClick = onOpenForm,
        )
        SgostSecondaryButton(
            text = "Cerrar sesion",
            onClick = onLogout,
        )
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, color = KtmOrange, fontWeight = FontWeight.Black)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
