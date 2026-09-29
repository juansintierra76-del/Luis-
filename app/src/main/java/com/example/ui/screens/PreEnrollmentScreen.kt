package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CurrencyMode
import com.example.data.models.School
import com.example.ui.theme.*
import com.example.util.PdfQuoteGenerator
import com.example.util.QuoteDetails

@Composable
fun PreEnrollmentScreen(
    school: School,
    currencyMode: CurrencyMode,
    onSubmitPreEnrollment: (weeks: Int, studentName: String, program: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var weeks by remember { mutableIntStateOf(12) }
    var selectedProgram by remember { mutableStateOf(school.popularPrograms.firstOrNull() ?: "General English") }
    var includeAccommodation by remember { mutableStateOf(true) }
    var includeInsurance by remember { mutableStateOf(true) }

    var studentName by remember { mutableStateOf("Juan Carlos Mendoza") }
    var studentEmail by remember { mutableStateOf("juansintierra76@gmail.com") }
    var studentPhone by remember { mutableStateOf("+51 987 654 321") }
    var peruvianRegion by remember { mutableStateOf("Lima Metropolitana") }

    var isSubmitted by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfResult by remember { mutableStateOf<PdfQuoteGenerator.PdfGenerationResult?>(null) }
    var showPdfSuccessDialog by remember { mutableStateOf(false) }

    // Cost calculations
    val exchangeRate = CurrencyMode.USD_TO_PEN_RATE
    val tuitionUsd = weeks * school.priceWeeklyUsd
    val registrationFeeUsd = 150.0
    val materialsFeeUsd = 80.0
    val accommodationUsd = if (includeAccommodation) weeks * 220.0 else 0.0
    val insuranceUsd = if (includeInsurance) 130.0 else 0.0

    val subtotalUsd = tuitionUsd + registrationFeeUsd + materialsFeeUsd + accommodationUsd + insuranceUsd
    val discountPercent = if (weeks >= 12) 0.10 else 0.0
    val discountUsd = subtotalUsd * discountPercent
    val totalUsd = subtotalUsd - discountUsd
    val totalPen = totalUsd * exchangeRate

    val weekPresets = listOf(4, 8, 12, 16, 24, 36, 48)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // School & Representative Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = school.flagEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = school.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${school.city}, ${school.country} • Acreditada",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Navy100
                        ) {
                            Text(
                                text = "Oficial 2026",
                                color = Navy800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Navy800, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Representante asignado a tu presupuesto:", fontSize = 10.sp, color = Slate500)
                                Text(
                                    text = "${school.representativeName} (${school.representativeRole})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cotizador & Configuración de Semanas
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cotizador Interactivo de Estudios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Navy800)
                    }

                    // Weeks presets
                    Text("Duración del curso:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(weekPresets) { w ->
                            val isSelected = weeks == w
                            FilterChip(
                                selected = isSelected,
                                onClick = { weeks = w },
                                label = {
                                    Text(
                                        text = "$w sem",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy800,
                                    selectedLabelColor = PureWhite
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Slider for fine tuning
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total semanas seleccionadas:", fontSize = 11.sp, color = Slate500)
                            Text("$weeks semanas (${weeks * 24} lecciones)", fontWeight = FontWeight.Bold, color = Navy800, fontSize = 12.sp)
                        }
                        Slider(
                            value = weeks.toFloat(),
                            onValueChange = { weeks = it.toInt() },
                            valueRange = 4f..48f,
                            steps = 10,
                            colors = SliderDefaults.colors(thumbColor = Navy800, activeTrackColor = Navy800)
                        )
                    }

                    HorizontalDivider(color = Slate200)

                    // Program Selector
                    Text("Programa académico:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                    school.popularPrograms.forEach { program ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedProgram == program) Navy50 else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selectedProgram == program) Navy800 else Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedProgram == program,
                                    onClick = { selectedProgram = program },
                                    colors = RadioButtonDefaults.colors(selectedColor = Navy800)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = program, fontSize = 13.sp, fontWeight = if (selectedProgram == program) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    HorizontalDivider(color = Slate200)

                    // Optional Services: Homestay & Insurance
                    Text("Servicios Adicionales Recomendados:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (includeAccommodation) Slate100 else MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeAccommodation,
                            onCheckedChange = { includeAccommodation = it },
                            colors = CheckboxDefaults.colors(checkedColor = Navy800)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alojamiento Homestay Familiar", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Habitación individual privada + Media pensión (2 comidas/día)", fontSize = 10.sp, color = Slate500)
                        }
                        Text("$ 220/sem", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy800)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (includeInsurance) Slate100 else MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeInsurance,
                            onCheckedChange = { includeInsurance = it },
                            colors = CheckboxDefaults.colors(checkedColor = Navy800)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Seguro Médico Internacional Estudiantil", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Póliza obligatoria para visa (OSHC / Guard.me)", fontSize = 10.sp, color = Slate500)
                        }
                        Text("$ 130 pago único", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy800)
                    }
                }
            }
        }

        // ============================================================
        // DESGLOSE DETALLADO DE COSTOS (USD & PEN)
        // ============================================================
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Desglose de Costos (USD & PEN)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate100
                        ) {
                            Text(
                                text = "TC: S/. 3.78",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Table Header
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Navy800,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Concepto", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Dólares ($)", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(85.dp), textAlign = TextAlign.End)
                            Text("Soles (S/.)", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Items rows
                    CostTableRow("Colegiatura ($weeks sem)", tuitionUsd, tuitionUsd * exchangeRate)
                    CostTableRow("Matrícula & Admisión", registrationFeeUsd, registrationFeeUsd * exchangeRate)
                    CostTableRow("Materiales Didácticos", materialsFeeUsd, materialsFeeUsd * exchangeRate)

                    if (includeAccommodation) {
                        CostTableRow("Homestay ($weeks sem)", accommodationUsd, accommodationUsd * exchangeRate)
                    }
                    if (includeInsurance) {
                        CostTableRow("Seguro Estudiantil", insuranceUsd, insuranceUsd * exchangeRate)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Slate200)

                    // Subtotal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate600, modifier = Modifier.weight(1f))
                        Text("$ ${"%.2f".format(subtotalUsd)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate600, modifier = Modifier.width(85.dp), textAlign = TextAlign.End)
                        Text("S/. ${"%.2f".format(subtotalUsd * exchangeRate)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate600, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                    }

                    // Promotion discount
                    if (discountUsd > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Descuento Promo Perú (10%):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Emerald700, modifier = Modifier.weight(1f))
                            Text("- $ ${"%.2f".format(discountUsd)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Emerald700, modifier = Modifier.width(85.dp), textAlign = TextAlign.End)
                            Text("- S/. ${"%.2f".format(discountUsd * exchangeRate)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Emerald700, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Final Total Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Navy900,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TOTAL OFICIAL:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PureWhite)
                                Text("Presupuesto para $weeks sem", fontSize = 10.sp, color = Slate200)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$ ${"%,.2f".format(totalUsd)} USD",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Gold500
                                )
                                Text(
                                    text = "≈ S/. ${"%,.2f".format(totalPen)} PEN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PeruRedLight
                                )
                            }
                        }
                    }

                    // ============================================================
                    // EXPORT TO PDF BUTTON
                    // ============================================================
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            val details = QuoteDetails(
                                studentName = studentName,
                                studentEmail = studentEmail,
                                studentPhone = studentPhone,
                                studentRegion = peruvianRegion,
                                programName = selectedProgram,
                                weeks = weeks,
                                includeAccommodation = includeAccommodation,
                                includeInsurance = includeInsurance,
                                discountPercent = if (weeks >= 12) 0.10 else 0.0
                            )

                            val res = PdfQuoteGenerator.generateQuotePdf(context, school, details)
                            isGeneratingPdf = false

                            res.onSuccess { r ->
                                pdfResult = r
                                showPdfSuccessDialog = true
                            }.onFailure { err ->
                                Toast.makeText(context, "Error al generar PDF: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy800,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_quote_pdf_button")
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando PDF Oficial...")
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Gold500)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar Presupuesto en PDF (USD & PEN)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Student Contact Info Form
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Datos del Estudiante para el Expediente", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Nombre y Apellidos") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = studentEmail,
                        onValueChange = { studentEmail = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = studentPhone,
                        onValueChange = { studentPhone = it },
                        label = { Text("Teléfono / WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = peruvianRegion,
                        onValueChange = { peruvianRegion = it },
                        label = { Text("Región / Ciudad en Perú") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Prematrícula registrada. El representante oficial ${school.representativeName} se comunicará contigo vía chat en vivo.",
                                color = Emerald700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                onSubmitPreEnrollment(weeks, studentName, selectedProgram)
                                isSubmitted = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_pre_enrollment_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Prematrícula y Abrir Expediente")
                        }
                    }
                }
            }
        }
    }

    // ============================================================
    // PDF GENERATION SUCCESS DIALOG
    // ============================================================
    if (showPdfSuccessDialog && pdfResult != null) {
        val result = pdfResult!!
        AlertDialog(
            onDismissRequest = { showPdfSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Emerald100,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DownloadDone, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Presupuesto PDF Descargado", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "El archivo PDF oficial ha sido generado y guardado en la carpeta de Documentos de tu dispositivo.",
                        fontSize = 12.sp,
                        color = Slate700
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Navy50,
                        border = BorderStroke(1.dp, Navy100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref:", fontSize = 11.sp, color = Slate500)
                                Text(result.quoteRef, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy800)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total USD:", fontSize = 11.sp, color = Slate500)
                                Text("$ ${"%,.2f".format(result.totalUsd)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy800)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total PEN:", fontSize = 11.sp, color = Slate500)
                                Text("S/. ${"%,.2f".format(result.totalPen)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PeruRed)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Archivo: ${result.file.name}",
                                fontSize = 10.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        PdfQuoteGenerator.openPdf(context, result.uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("open_generated_pdf_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        PdfQuoteGenerator.sharePdf(context, result.uri, school.name)
                    },
                    modifier = Modifier.testTag("share_generated_pdf_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir")
                }
            }
        )
    }
}

@Composable
private fun CostTableRow(title: String, usdAmount: Double, penAmount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 11.sp, color = Slate700, modifier = Modifier.weight(1f))
        Text("$ ${"%.2f".format(usdAmount)}", fontSize = 11.sp, color = Slate800, modifier = Modifier.width(85.dp), textAlign = TextAlign.End)
        Text("S/. ${"%.2f".format(penAmount)}", fontSize = 11.sp, color = Slate800, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
    }
}
