package com.example.data.models

enum class VisaStatus(
    val title: String,
    val shortDesc: String,
    val stepOrder: Int,
    val progressPercent: Float
) {
    RECOPILANDO_DOCUMENTOS(
        title = "Recopilando documentos",
        shortDesc = "Reuniendo pasaporte, solvencia económica y carta de motivos.",
        stepOrder = 1,
        progressPercent = 0.15f
    ),
    DOCUMENTOS_ENVIADOS(
        title = "Documentos enviados",
        shortDesc = "Expediente oficial ingresado a la plataforma consular / embajada.",
        stepOrder = 2,
        progressPercent = 0.35f
    ),
    BIOMETRICOS_PENDIENTES(
        title = "Biométricos pendientes",
        shortDesc = "Esperando cita de toma de huellas dactilares y fotografía en VFS Global.",
        stepOrder = 3,
        progressPercent = 0.50f
    ),
    CITA_PROGRAMADA(
        title = "Cita programada",
        shortDesc = "Fecha fijada para entrevista o entrega de pasaporte en el consulado.",
        stepOrder = 4,
        progressPercent = 0.70f
    ),
    EN_EVALUACION(
        title = "En evaluación consular",
        shortDesc = "El oficial de inmigración está analizando tu solicitud académica.",
        stepOrder = 5,
        progressPercent = 0.85f
    ),
    APROBADO(
        title = "Aprobado",
        shortDesc = "¡Visa concedida exitosamente! Pasaporte visado y listo para viajar.",
        stepOrder = 6,
        progressPercent = 1.0f
    ),
    REQUIERE_SUBSANACION(
        title = "Requiere subsanación",
        shortDesc = "El consulado solicita aclaración de fondos o documentos adicionales.",
        stepOrder = 3,
        progressPercent = 0.40f
    )
}

data class VisaRequirementItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isMandatory: Boolean = true
)

data class VisaStatusUpdate(
    val id: String,
    val status: VisaStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val updatedBy: String = "Estudiante (Tú)"
)

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val country: String = ""
)

data class VisaApplication(
    val id: String,
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val visaType: String,
    val schoolName: String,
    val trackingNumber: String,
    val currentStatus: VisaStatus,
    val appointmentDate: String? = null,
    val appointmentLocation: String? = null,
    val embassyName: String,
    val embassyAddressInLima: String,
    val officialHelpline: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val checklist: List<VisaRequirementItem> = emptyList(),
    val history: List<VisaStatusUpdate> = emptyList(),
    val schoolId: String = "",
    val programName: String = "General English Intensive",
    val courseDuration: String = "24 Semanas (6 Meses)",
    val startDate: String = "02 de Febrero, 2026",
    val financialProofRequired: String = "Demostración de fondos oficiales",
    val estimatedProcessingTime: String = "4 a 6 semanas"
)
