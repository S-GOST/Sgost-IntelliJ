package org.sgost.project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sgost.project.domain.model.ServiceRequest
import org.sgost.project.presentation.components.SgostPrimaryButton
import org.sgost.project.presentation.components.SgostSecondaryButton
import org.sgost.project.presentation.components.SgostTextField
import org.sgost.project.presentation.theme.KtmOrange
import org.sgost.project.utils.isNotBlankValue

@Composable
fun ServiceFormScreen(
    onBack: () -> Unit,
) {
    var clientName by remember { mutableStateOf("") }
    var motorcycle by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var savedRequest by remember { mutableStateOf<ServiceRequest?>(null) }
    val canSave = clientName.isNotBlankValue() && motorcycle.isNotBlankValue() && serviceType.isNotBlankValue()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Formulario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Registro de servicio Sgost",
            color = KtmOrange,
            fontWeight = FontWeight.Bold,
        )

        SgostTextField(
            value = clientName,
            onValueChange = {
                clientName = it
                savedRequest = null
            },
            label = "Cliente",
        )
        SgostTextField(
            value = motorcycle,
            onValueChange = {
                motorcycle = it
                savedRequest = null
            },
            label = "Moto / placa",
        )
        SgostTextField(
            value = serviceType,
            onValueChange = {
                serviceType = it
                savedRequest = null
            },
            label = "Tipo de servicio",
        )
        SgostTextField(
            value = notes,
            onValueChange = {
                notes = it
                savedRequest = null
            },
            label = "Notas",
            singleLine = false,
        )

        savedRequest?.let {
            Text(
                text = "Solicitud guardada: ${it.clientName} - ${it.motorcycle}",
                color = KtmOrange,
                fontWeight = FontWeight.Black,
            )
        }

        SgostPrimaryButton(
            text = "GUARDAR FORMULARIO",
            enabled = canSave,
            onClick = {
                savedRequest = ServiceRequest(
                    clientName = clientName,
                    motorcycle = motorcycle,
                    serviceType = serviceType,
                    notes = notes,
                )
            },
        )
        SgostSecondaryButton(
            text = "Volver al panel",
            onClick = onBack,
        )
    }
}
