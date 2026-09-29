package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.data.models.CurrencyMode
import com.example.data.models.MessageSenderRole
import com.example.data.models.MessageStatus
import com.example.data.models.School
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeChatScreen(
    currentSchool: School,
    allSchools: List<School>,
    messages: List<ChatMessage>,
    currencyMode: CurrencyMode,
    onSendMessage: (String) -> Unit,
    onSelectSchool: (School) -> Unit,
    onPreEnrollClick: (School) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showSchoolSelectorSheet by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var isDownloadingPdfInChat by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = remember(currentSchool) {
        listOf(
            "📋 ¿Cuáles son los requisitos de visa para ${currentSchool.country}?",
            "💰 ¿Cuánto cuesta en Soles (PEN) un curso de 12 semanas?",
            "🏠 ¿Tienen opciones de alojamiento en homestay familiar?",
            if (currentSchool.visaAllowsWork) "💼 ¿Cómo funciona el permiso de trabajo de 48h/quincena?"
            else "📅 ¿Cuáles son las fechas de inicio para 2026?",
            "📄 ¿En cuánto tiempo emiten la Carta de Aceptación (LOA)?"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ============================================================
        // 1. OFFICIAL REPRESENTATIVE HEADER
        // ============================================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Representative Info & Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                shape = CircleShape,
                                color = Navy800,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentSchool.representativeName.take(2).uppercase(),
                                        color = Gold500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                            }
                            // Online Indicator Dot
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(Emerald600)
                                    .padding(2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentSchool.representativeName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald100,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Verificado",
                                            tint = Emerald700,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${currentSchool.representativeRole} • ${currentSchool.name}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "● En línea en Firestore",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Emerald700
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${currentSchool.city} (${currentSchool.representativeTimezone})",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    // School Switcher & Pre-enroll Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSchoolSelectorSheet = true },
                            modifier = Modifier.testTag("switch_school_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Cambiar de Escuela",
                                tint = Navy800
                            )
                        }

                        FilledTonalButton(
                            onClick = { onPreEnrollClick(currentSchool) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Navy100,
                                contentColor = Navy800
                            ),
                            modifier = Modifier.testTag("chat_pre_enroll_button")
                        ) {
                            Text("Prematrícula", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Cloud Status & School Details Strip
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentSchool.flagEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentSchool.name} • ${currentSchool.city}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Desde ${CurrencyMode.formatPrice(currentSchool.priceWeeklyUsd, currencyMode)}/sem",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy800
                        )
                    }
                }
            }
        }

        // ============================================================
        // 2. QUICK INQUIRY CHIPS ROW
        // ============================================================
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickQuestions) { question ->
                SuggestionChip(
                    onClick = {
                        onSendMessage(question)
                    },
                    label = {
                        Text(
                            text = question,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = PureWhite,
                        labelColor = Navy800
                    ),
                    border = BorderStroke(1.dp, Navy100),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_question_chip")
                )
            }
        }

        // ============================================================
        // 3. REAL-TIME MESSAGES LIST (LAZYCOLUMN)
        // ============================================================
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
            // Encryption & Cloud Guarantee Notice
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Gold100.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Gold600,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Canal oficial directo con el representante de ${currentSchool.name}. Sincronizado en tiempo real con Firestore.",
                                fontSize = 10.sp,
                                color = Slate700,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    school = currentSchool,
                    onOpenAttachment = {
                        showAttachmentDialog = true
                    }
                )
            }
        }

        // ============================================================
        // 4. BOTTOM INPUT BAR WITH SEND & ATTACHMENT
        // ============================================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Action
                    IconButton(
                        onClick = { showAttachmentDialog = true },
                        modifier = Modifier.testTag("chat_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = "Adjuntar archivo o cotización",
                            tint = Navy800
                        )
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Consulta a ${currentSchool.representativeName}...",
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy800,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Navy800,
                            disabledContainerColor = Slate200
                        ),
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = if (inputText.isNotBlank()) PureWhite else Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // ============================================================
    // 5. SCHOOL SELECTOR DIALOG (SWITCH CONVERSATION)
    // ============================================================
    if (showSchoolSelectorSheet) {
        AlertDialog(
            onDismissRequest = { showSchoolSelectorSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = Navy800,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Representantes Oficiales",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    Text(
                        text = "Elige una escuela de inglés para iniciar o continuar tu conversación directa con su director o asesor:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allSchools) { school ->
                            val isSelected = school.id == currentSchool.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Navy100 else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) Navy800 else Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectSchool(school)
                                        showSchoolSelectorSheet = false
                                    }
                                    .testTag("select_chat_school_${school.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) Navy800 else Slate200,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = school.flagEmoji,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = school.representativeName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Navy800 else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${school.name} • ${school.city}",
                                                fontSize = 11.sp,
                                                color = Slate600,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Navy800
                                        ) {
                                            Text(
                                                text = "Activo",
                                                color = PureWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSchoolSelectorSheet = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // ============================================================
    // 6. ATTACHMENT DIALOG (BROCHURES & LOA DOCS)
    // ============================================================
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = {
                Text("Documentos & Brochures Oficiales")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Documentación compartida por el departamento de admisiones de ${currentSchool.name}:",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Navy50,
                        border = BorderStroke(1.dp, Navy100),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PeruRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Brochure Académico y Lista de Precios 2026", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("PDF Oficial • 4.2 MB • Acreditado por ${currentSchool.accreditation}", fontSize = 10.sp, color = Slate500)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald100.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Guía de Visado y Permiso Laboral", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Requisitos oficiales para estudiantes peruanos", fontSize = 10.sp, color = Slate500)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            isDownloadingPdfInChat = true
                            val details = com.example.util.QuoteDetails(
                                studentName = "Juan Carlos Mendoza",
                                studentEmail = "juansintierra76@gmail.com",
                                studentPhone = "+51 987 654 321",
                                studentRegion = "Lima, Perú",
                                programName = currentSchool.popularPrograms.firstOrNull() ?: "General English",
                                weeks = 12,
                                includeAccommodation = true,
                                includeInsurance = true,
                                discountPercent = 0.10
                            )
                            val res = com.example.util.PdfQuoteGenerator.generateQuotePdf(context, currentSchool, details)
                            isDownloadingPdfInChat = false
                            res.onSuccess { r ->
                                com.example.util.PdfQuoteGenerator.openPdf(context, r.uri)
                                showAttachmentDialog = false
                            }.onFailure { err ->
                                android.widget.Toast.makeText(context, "Error: ${err.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("download_quote_pdf_from_chat")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Descargar Presupuesto PDF (USD & PEN)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAttachmentDialog = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Single Chat Message Bubble Composable (Student vs Representative)
 */
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    school: School,
    onOpenAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFromStudent = message.senderRole == MessageSenderRole.STUDENT
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromStudent) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromStudent) {
            // Representative Avatar
            Surface(
                shape = CircleShape,
                color = Navy800,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = school.representativeName.take(1).uppercase(),
                        color = Gold500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isFromStudent) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            // Sender Name and Role Label (for Representative)
            if (!isFromStudent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Navy800
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Representante",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
            }

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromStudent) 16.dp else 4.dp,
                    bottomEnd = if (isFromStudent) 4.dp else 16.dp
                ),
                color = if (isFromStudent) Navy800 else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (isFromStudent) PureWhite else MaterialTheme.colorScheme.onSurface
                    )

                    // Optional Attachment Card
                    if (message.attachmentTitle != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFromStudent) Navy900 else PureWhite,
                            border = BorderStroke(1.dp, if (isFromStudent) Navy700 else Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenAttachment() }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = if (isFromStudent) Gold500 else PeruRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.attachmentTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFromStudent) PureWhite else Slate800,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Toca para abrir documento",
                                        fontSize = 9.sp,
                                        color = if (isFromStudent) Slate200 else Slate500
                                    )
                                }
                            }
                        }
                    }

                    // Timestamp and delivery status ticks
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = if (isFromStudent) Slate200 else Slate500
                        )

                        if (isFromStudent) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = when (message.status) {
                                    MessageStatus.READ -> Icons.Default.DoneAll
                                    MessageStatus.DELIVERED -> Icons.Default.DoneAll
                                    MessageStatus.SENT -> Icons.Default.Done
                                    MessageStatus.SENDING -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                tint = if (message.status == MessageStatus.READ) Cyan100 else Slate200,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
