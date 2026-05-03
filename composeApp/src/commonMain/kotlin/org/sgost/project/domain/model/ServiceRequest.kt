package org.sgost.project.domain.model

data class ServiceRequest(
    val clientName: String,
    val motorcycle: String,
    val serviceType: String,
    val notes: String,
)
