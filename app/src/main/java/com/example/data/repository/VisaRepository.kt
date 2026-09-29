package com.example.data.repository

import com.example.data.models.VisaApplication
import com.example.data.models.VisaRequirementItem
import com.example.data.models.VisaStatus
import com.example.data.models.VisaStatusUpdate

class VisaRepository {

    fun getInitialApplications(): List<VisaApplication> {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000)
        val fiveDaysAgo = now - (5 * 24 * 60 * 60 * 1000)
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)

        return listOf(
            // 1. Australia
            VisaApplication(
                id = "visa_aus_1",
                schoolId = "ilsc_melbourne",
                country = "Australia",
                countryCode = "AU",
                flagEmoji = "🇦🇺",
                visaType = "Student Visa (Subclass 500)",
                schoolName = "ILSC Language Schools Melbourne",
                programName = "General English (Intensive 24h/sem)",
                courseDuration = "24 Semanas (6 Meses)",
                startDate = "02 de Febrero, 2026",
                financialProofRequired = "AUD $29,710 / año de fondos demostrables",
                estimatedProcessingTime = "4 a 6 semanas",
                trackingNumber = "PER-AUS-2026-89421",
                currentStatus = VisaStatus.DOCUMENTOS_ENVIADOS,
                appointmentDate = "14 de Octubre, 2026 - 09:30 AM",
                appointmentLocation = "VFS Global Lima - Av. Javier Prado Este 4200, Santiago de Surco",
                embassyName = "Embajada de Australia en el Perú / VFS Global",
                embassyAddressInLima = "Av. La Paz 1049, Piso 10, Miraflores, Lima",
                officialHelpline = "+51 1 630 0500 • immigration.peru@dfat.gov.au",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("aus_chk_1", "Pasaporte peruano vigente", "Mínimo 6 meses de vigencia antes del viaje.", isCompleted = true),
                    VisaRequirementItem("aus_chk_2", "Carta de Aceptación (CoE de ILSC)", "Confirmation of Enrolment oficial emitido por la escuela.", isCompleted = true),
                    VisaRequirementItem("aus_chk_3", "Seguro médico obligatorio (OSHC)", "Cobertura médica estudiantil Bupa o Medibank.", isCompleted = true),
                    VisaRequirementItem("aus_chk_4", "Sustento de solvencia económica", "Extractos bancarios de los últimos 3 meses (auditoría).", isCompleted = false),
                    VisaRequirementItem("aus_chk_5", "Examen médico con médico panel", "Rayos X y análisis de orina en clínica autorizada en Lima.", isCompleted = false)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_aus_1", VisaStatus.RECOPILANDO_DOCUMENTOS, fiveDaysAgo, "Inicio de preparación de documentos y cotización con asesor."),
                    VisaStatusUpdate("hist_aus_2", VisaStatus.DOCUMENTOS_ENVIADOS, twoDaysAgo, "Expediente digital enviado a través del portal oficial ImmiAccount de Australia.")
                )
            ),

            // 2. Canadá
            VisaApplication(
                id = "visa_can_2",
                schoolId = "ilac_toronto",
                country = "Canadá",
                countryCode = "CA",
                flagEmoji = "🇨🇦",
                visaType = "Study Permit (IMM 1294)",
                schoolName = "ILAC - International Language Academy of Canada",
                programName = "University Pathway Program (38 lecciones/sem)",
                courseDuration = "32 Semanas (8 Meses)",
                startDate = "16 de Marzo, 2026",
                financialProofRequired = "CAD $20,635 / año de manutención + costo del curso",
                estimatedProcessingTime = "6 a 9 semanas",
                trackingNumber = "PER-CAN-2026-55102",
                currentStatus = VisaStatus.RECOPILANDO_DOCUMENTOS,
                appointmentDate = "Pendiente de cita biométrica",
                appointmentLocation = "VAC Centro de Solicitud de Visas de Canadá - Lima",
                embassyName = "Embajada de Canadá en Lima",
                embassyAddressInLima = "Bolognesi 228, Miraflores, Lima",
                officialHelpline = "+51 1 319 3200",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("can_chk_1", "Carta de Aceptación Oficial (LOA)", "Documento emitido por ILAC Toronto DLI #O193756382.", isCompleted = true),
                    VisaRequirementItem("can_chk_2", "Provincial Attestation Letter (PAL)", "Carta de certificación del gobierno provincial de Ontario.", isCompleted = false),
                    VisaRequirementItem("can_chk_3", "Demostración de fondos económicos", "Mínimo CAD $20,635 por año de estadía según IRCC.", isCompleted = false),
                    VisaRequirementItem("can_chk_4", "Carta de Explicación / Study Plan", "Motivos para regresar a Perú tras finalizar el curso.", isCompleted = true)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_can_1", VisaStatus.RECOPILANDO_DOCUMENTOS, twoDaysAgo, "Reuniendo LOA y carta de explicación.")
                )
            ),

            // 3. USA
            VisaApplication(
                id = "visa_usa_3",
                schoolId = "ec_new_york",
                country = "USA",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                visaType = "F-1 Academic Student Visa",
                schoolName = "EC English Language Centres Times Square",
                programName = "Academic English I-20 (30 lecciones/sem)",
                courseDuration = "24 Semanas (6 Meses)",
                startDate = "06 de Abril, 2026",
                financialProofRequired = "$22,000 USD de solvencia financiera",
                estimatedProcessingTime = "2 a 4 semanas tras entrevista",
                trackingNumber = "PER-USA-2026-31908",
                currentStatus = VisaStatus.CITA_PROGRAMADA,
                appointmentDate = "22 de Octubre, 2026 - 10:15 AM",
                appointmentLocation = "Embajada de EE.UU. en Lima - Av. La Encalada cdra. 17, Surco",
                embassyName = "Embajada de los Estados Unidos en Lima",
                embassyAddressInLima = "Avenida La Encalada cuadra 17 s/n, Monterrico, Surco, Lima",
                officialHelpline = "+51 1 709 7950",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("usa_chk_1", "Formulario I-20 emitido por EC New York", "Firmado por la DSO de la escuela.", isCompleted = true),
                    VisaRequirementItem("usa_chk_2", "Pago de tarifa SEVIS I-901 ($350 USD)", "Recibo oficial impreso de fmjfee.com.", isCompleted = true),
                    VisaRequirementItem("usa_chk_3", "Confirmación de Formulario DS-160", "Página de confirmación con código de barras.", isCompleted = true),
                    VisaRequirementItem("usa_chk_4", "Constancia de cita consular programada", "Confirmación de fecha y hora para ventanilla.", isCompleted = true)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_usa_1", VisaStatus.RECOPILANDO_DOCUMENTOS, now - 600000000, "I-20 recibido de la escuela."),
                    VisaStatusUpdate("hist_usa_2", VisaStatus.DOCUMENTOS_ENVIADOS, now - 400000000, "Formulario DS-160 enviado online."),
                    VisaStatusUpdate("hist_usa_3", VisaStatus.CITA_PROGRAMADA, now - 100000000, "Cita agendada para el 22 de Octubre.")
                )
            ),

            // 4. Irlanda
            VisaApplication(
                id = "visa_irl_4",
                schoolId = "ces_dublin",
                country = "Irlanda",
                countryCode = "IE",
                flagEmoji = "🇮🇪",
                visaType = "Stamp 2 Student Visa (ILEP Work & Study)",
                schoolName = "Centre of English Studies (CES) Dublin",
                programName = "25-Week Work & Study Ireland (20h/sem)",
                courseDuration = "25 Semanas curso + 8 Semanas vacaciones",
                startDate = "11 de Mayo, 2026",
                financialProofRequired = "€4,500 EUR para estudiantes peruanos no-visa",
                estimatedProcessingTime = "Registro directo en Dublín (GNIB)",
                trackingNumber = "PER-IRL-2026-44091",
                currentStatus = VisaStatus.DOCUMENTOS_ENVIADOS,
                appointmentDate = "Registro presencial a la llegada en Burgh Quay, Dublín",
                appointmentLocation = "Consulado Honorario de Irlanda en Lima / Registro INIS Dublín",
                embassyName = "Consulado de Irlanda en Lima",
                embassyAddressInLima = "Av. Rivera Navarrete 501, San Isidro, Lima",
                officialHelpline = "+51 1 422 7100",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("irl_chk_1", "Carta de Aceptación ILEP de CES Dublín", "Certificado de pago del 100% de la matrícula.", isCompleted = true),
                    VisaRequirementItem("irl_chk_2", "Seguro médico gubernamental (Medicover)", "Póliza anual obligatoria para visado Stamp 2.", isCompleted = true),
                    VisaRequirementItem("irl_chk_3", "Demostración de fondos (€4,500)", "Extracto bancario internacional a nombre del estudiante.", isCompleted = true),
                    VisaRequirementItem("irl_chk_4", "Reserva de Alojamiento inicial (4 semanas)", "Homestay o residencia confirmada en Dublín.", isCompleted = false)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_irl_1", VisaStatus.RECOPILANDO_DOCUMENTOS, sevenDaysAgo, "Expediente de matrícula completado con CES Dublín."),
                    VisaStatusUpdate("hist_irl_2", VisaStatus.DOCUMENTOS_ENVIADOS, twoDaysAgo, "Póliza médica y carta ILEP emitidas.")
                )
            ),

            // 5. England
            VisaApplication(
                id = "visa_uk_5",
                schoolId = "kaplan_london",
                country = "England",
                countryCode = "GB",
                flagEmoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                visaType = "Standard Visitor Visa / Short-term Study",
                schoolName = "Kaplan International Covent Garden",
                programName = "Intensive Academic Semester London (28h/sem)",
                courseDuration = "20 Semanas (5 Meses)",
                startDate = "01 de Junio, 2026",
                financialProofRequired = "Fondos suficientes para estadía en Londres (£1,334/mes)",
                estimatedProcessingTime = "Sin visado previo para estancias < 6 meses",
                trackingNumber = "PER-UK-2026-11893",
                currentStatus = VisaStatus.APROBADO,
                appointmentDate = "No requerida (Exención para peruanos < 6 meses)",
                appointmentLocation = "Control migratorio en Heathrow Airport, Londres",
                embassyName = "Embajada Británica en Lima",
                embassyAddressInLima = "Torre Parque Mar, Av. José Larco 1301, Piso 22, Miraflores, Lima",
                officialHelpline = "+51 1 617 3000",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("uk_chk_1", "Carta de Matrícula Kaplan Covent Garden", "Confirmación de curso intensivo presencial.", isCompleted = true),
                    VisaRequirementItem("uk_chk_2", "Pasaporte biométrico vigente", "Con vigencia durante toda la estancia en Reino Unido.", isCompleted = true),
                    VisaRequirementItem("uk_chk_3", "Alojamiento en Londres confirmado", "Residencia o homestay verificado por la escuela.", isCompleted = true),
                    VisaRequirementItem("uk_chk_4", "Boleto aéreo ida y vuelta Lima-Londres", "Con fecha de regreso antes de los 180 días.", isCompleted = true)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_uk_1", VisaStatus.RECOPILANDO_DOCUMENTOS, now - 800000000, "Revisión de requisitos sin visado previo."),
                    VisaStatusUpdate("hist_uk_2", VisaStatus.DOCUMENTOS_ENVIADOS, now - 500000000, "Expediente académico validado por Kaplan UK."),
                    VisaStatusUpdate("hist_uk_3", VisaStatus.APROBADO, now - 100000000, "Documentos listos para ingreso sin visa por Heathrow.")
                )
            ),

            // 6. New Zealand
            VisaApplication(
                id = "visa_nz_6",
                schoolId = "nzlc_auckland",
                country = "New Zealand",
                countryCode = "NZ",
                flagEmoji = "🇳🇿",
                visaType = "Fee Paying Student Visa",
                schoolName = "NZLC - New Zealand Language Centres Auckland",
                programName = "General English Full-time (Permiso laboral)",
                courseDuration = "24 Semanas (6 Meses)",
                startDate = "13 de Julio, 2026",
                financialProofRequired = "NZD $1,667 por mes de estudio en Nueva Zelanda",
                estimatedProcessingTime = "5 a 8 semanas",
                trackingNumber = "PER-NZ-2026-77204",
                currentStatus = VisaStatus.EN_EVALUACION,
                appointmentDate = "En revisión online por Immigration New Zealand",
                appointmentLocation = "Immigration New Zealand Online Portal / VFS Global Lima",
                embassyName = "Consulado Honorario de Nueva Zelanda en Lima",
                embassyAddressInLima = "Av. Víctor Andrés Belaúnde 147, Real Seis, San Isidro, Lima",
                officialHelpline = "+51 1 222 5555",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("nz_chk_1", "Offer of Place oficial de NZLC", "Escuela Categoría 1 con derecho a trabajo.", isCompleted = true),
                    VisaRequirementItem("nz_chk_2", "Recibo oficial de pago de colegiatura", "Certificación bancaria internacional emitida por NZLC.", isCompleted = true),
                    VisaRequirementItem("nz_chk_3", "Examen médico eMedical", "Rayos X realizados en clínica autorizada de Lima.", isCompleted = true),
                    VisaRequirementItem("nz_chk_4", "Certificado de antecedentes policiales", "Legalizado ante cancillería de Perú.", isCompleted = true)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_nz_1", VisaStatus.RECOPILANDO_DOCUMENTOS, now - 700000000, "Oferta de cupo aceptada en Auckland."),
                    VisaStatusUpdate("hist_nz_2", VisaStatus.DOCUMENTOS_ENVIADOS, now - 400000000, "Solicitud online ingresada a INZ."),
                    VisaStatusUpdate("hist_nz_3", VisaStatus.EN_EVALUACION, now - 100000000, "Oficial de inmigración asignado para resolución.")
                )
            ),

            // 7. Malta
            VisaApplication(
                id = "visa_mlt_7",
                schoolId = "ec_malta",
                country = "Malta",
                countryCode = "MT",
                flagEmoji = "🇲🇹",
                visaType = "National D Visa / Schengen Long Stay",
                schoolName = "EC English Language Centres St. Julian's",
                programName = "Intensive English 30 + Permiso de Trabajo",
                courseDuration = "24 Semanas (6 Meses)",
                startDate = "07 de Septiembre, 2026",
                financialProofRequired = "€25 EUR por día de estadía si tiene alojamiento",
                estimatedProcessingTime = "4 a 6 semanas",
                trackingNumber = "PER-MLT-2026-90432",
                currentStatus = VisaStatus.BIOMETRICOS_PENDIENTES,
                appointmentDate = "18 de Octubre, 2026 - 11:00 AM",
                appointmentLocation = "VFS Global Malta / Central Visa Unit Lima",
                embassyName = "Consulado Honorario de Malta en Lima",
                embassyAddressInLima = "Av. Larco 101, Piso 7, Miraflores, Lima",
                officialHelpline = "+51 1 445 8890",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("mlt_chk_1", "Carta de Admisión de EC Malta", "Con sello de FELTOM y Ministerio de Educación.", isCompleted = true),
                    VisaRequirementItem("mlt_chk_2", "Seguro de viaje Schengen €30,000", "Con repatriación y cobertura médica total.", isCompleted = true),
                    VisaRequirementItem("mlt_chk_3", "Copia de contrato de alquiler o residencia", "Aprobado por Housing Authority Malta.", isCompleted = false)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_mlt_1", VisaStatus.RECOPILANDO_DOCUMENTOS, now - 500000000, "Inscripción en EC Malta confirmada."),
                    VisaStatusUpdate("hist_mlt_2", VisaStatus.BIOMETRICOS_PENDIENTES, now - 150000000, "Cita para huellas dactilares programada.")
                )
            ),

            // 8. Sudáfrica
            VisaApplication(
                id = "visa_za_8",
                schoolId = "good_hope_capetown",
                country = "Sudáfrica",
                countryCode = "ZA",
                flagEmoji = "🇿🇦",
                visaType = "Study Visa (Sección 13)",
                schoolName = "Good Hope Studies City Centre",
                programName = "English Fluency & Safari Experience",
                courseDuration = "16 Semanas (4 Meses)",
                startDate = "05 de Octubre, 2026",
                financialProofRequired = "R 3,000 ZAR por mes de estadía en Sudáfrica",
                estimatedProcessingTime = "4 a 6 semanas",
                trackingNumber = "PER-ZAF-2026-66381",
                currentStatus = VisaStatus.RECOPILANDO_DOCUMENTOS,
                appointmentDate = "Pendiente de presentación consular",
                appointmentLocation = "Embajada de Sudáfrica en Lima",
                embassyName = "Embajada de la República de Sudáfrica en el Perú",
                embassyAddressInLima = "Av. Víctor Andrés Belaúnde 147, Edificio Real Tres, Oficina 801, San Isidro, Lima",
                officialHelpline = "+51 1 612 4848 • general.peru@dirco.gov.za",
                lastUpdated = now,
                checklist = listOf(
                    VisaRequirementItem("za_chk_1", "Carta de Oferta Oficial Good Hope Studies", "Con fecha de inicio y finalización del curso.", isCompleted = true),
                    VisaRequirementItem("za_chk_2", "Certificado de Vacunación Fiebre Amarilla", "Carnet internacional oficial del MINSA Perú.", isCompleted = true),
                    VisaRequirementItem("za_chk_3", "Certificado médico y radiológico (BI-811)", "Comprobante de salud física y rayos X de tórax.", isCompleted = false),
                    VisaRequirementItem("za_chk_4", "Depósito de repatriación o boleto de retorno", "Garantía de salida al finalizar el periodo.", isCompleted = false)
                ),
                history = listOf(
                    VisaStatusUpdate("hist_za_1", VisaStatus.RECOPILANDO_DOCUMENTOS, twoDaysAgo, "Inicio de preparación médica y radiológica.")
                )
            )
        )
    }
}
