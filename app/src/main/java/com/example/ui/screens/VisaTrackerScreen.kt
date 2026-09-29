package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.util.VisaSyncStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaTrackerScreen(
    applications: List<VisaApplication>,
    activeApplication: VisaApplication,
    inAppNotifications: List<InAppNotification>,
    latestNotification: InAppNotification?,
    syncStatus: VisaSyncStatus,
    onSelectApplication: (VisaApplication) -> Unit,
    onUpdateStatus: (VisaApplication, VisaStatus, String, String?) -> Unit,
    onToggleChecklistItem: (VisaApplication, String) -> Unit,
    onAddChecklistItem: (VisaApplication, String, String) -> Unit,
    onRefreshSync: () -> Unit,
    onDismissNotificationBanner: () -> Unit,
    onChatWithSchool: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUpdateStatusDialog by remember { mutableStateOf(false) }
    var presetStatusForDialog by remember { mutableStateOf<VisaStatus?>(null) }
    var showNotificationsHistoryDialog by remember { mutableStateOf(false) }
    var showAddDocumentDialog by remember { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ============================================================
            // 1. REAL-TIME NOTIFICATION BANNER (ANIMATED POPDOWN)
            // ============================================================
            AnimatedVisibility(
                visible = latestNotification != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                if (latestNotification != null) {
                    Surface(
                        color = Navy900,
                        contentColor = PureWhite,
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showNotificationsHistoryDialog = true }
                            .testTag("in_app_notification_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Gold500,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Navy900,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = latestNotification.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Gold500
                                )
                                Text(
                                    text = latestNotification.message,
                                    fontSize = 11.sp,
                                    color = PureWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = onDismissNotificationBanner,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar notificación",
                                    tint = Slate200,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ============================================================
            // 2. DASHBOARD HEADER & FIRESTORE CLOUD STATUS
            // ============================================================
            Surface(
                color = PureWhite,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Navy800,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AirplaneTicket,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Visa Status Dashboard",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Navy900
                                )
                                Text(
                                    text = "Monitoreo en tiempo real de tu programa",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        // Notifications History button
                        IconButton(
                            onClick = { showNotificationsHistoryDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("view_notifications_history_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (inAppNotifications.isNotEmpty()) {
                                        Badge(
                                            containerColor = PeruRed,
                                            contentColor = PureWhite
                                        ) {
                                            Text(inAppNotifications.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Historial de Notificaciones",
                                    tint = Navy800
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cloud Firestore Real-time status pill
                    Surface(
                        color = if (syncStatus.isConnected) Emerald100 else Slate100,
                        border = BorderStroke(1.dp, if (syncStatus.isConnected) Emerald600.copy(alpha = 0.3f) else Slate300),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (syncStatus.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = Navy800
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (syncStatus.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (syncStatus.isConnected) Emerald700 else Slate600,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = syncStatus.lastSyncMessage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (syncStatus.isConnected) Emerald700 else Slate700,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = onRefreshSync,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = Navy800)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Sincronizar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Navy800)
                            }
                        }
                    }
                }
            }

            // ============================================================
            // 3. STUDY PROGRAM SELECTOR CAROUSEL
            // ============================================================
            Surface(
                color = Slate50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = "PROGRAMA DE ESTUDIOS SELECCIONADO:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(applications) { app ->
                            val isSelected = app.id == activeApplication.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Navy800 else PureWhite,
                                border = BorderStroke(1.dp, if (isSelected) Navy800 else Slate200),
                                shadowElevation = if (isSelected) 2.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectApplication(app) }
                                    .testTag("visa_program_chip_${app.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = app.flagEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = app.country,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) PureWhite else Slate900
                                        )
                                        Text(
                                            text = app.programName,
                                            fontSize = 9.sp,
                                            color = if (isSelected) Gold500 else Slate500,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ============================================================
            // 4. MAIN SCROLLABLE DASHBOARD CONTENT
            // ============================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
            ) {
                // Selected Study Program Overview Card
                item {
                    SelectedStudyProgramCard(
                        application = activeApplication,
                        onChatClick = { onChatWithSchool(activeApplication.schoolId) }
                    )
                }

                // Visa Application Status & Progress Card
                item {
                    VisaApplicationHeroCard(
                        application = activeApplication,
                        onUpdateStatusClick = {
                            presetStatusForDialog = null
                            showUpdateStatusDialog = true
                        }
                    )
                }

                // Quick Action Bar: 1-Tap Document Stages to Firestore
                item {
                    QuickDocumentStageBar(
                        currentStatus = activeApplication.currentStatus,
                        onSelectStage = { stage ->
                            presetStatusForDialog = stage
                            showUpdateStatusDialog = true
                        }
                    )
                }

                // Visa Process Timeline / Stepper with Direct Stage Marking
                item {
                    VisaJourneyTimelineCard(
                        currentStatus = activeApplication.currentStatus,
                        onMarkStage = { stage ->
                            presetStatusForDialog = stage
                            showUpdateStatusDialog = true
                        }
                    )
                }

                // Requirements Checklist with Cloud Firestore sync
                item {
                    VisaRequirementsChecklistCard(
                        checklist = activeApplication.checklist,
                        onToggle = { itemId -> onToggleChecklistItem(activeApplication, itemId) },
                        onAddDocumentClick = { showAddDocumentDialog = true }
                    )
                }

                // Stage History Logs stored in Firestore
                item {
                    VisaHistoryLogCard(history = activeApplication.history)
                }

                // Consular Office Details in Lima
                item {
                    ConsularOfficeCard(application = activeApplication)
                }
            }
        }
    }

    // ============================================================
    // 5. UPDATE STATUS / STAGE MODAL DIALOG
    // ============================================================
    if (showUpdateStatusDialog) {
        UpdateVisaStatusDialog(
            currentApplication = activeApplication,
            initialStatus = presetStatusForDialog ?: activeApplication.currentStatus,
            onDismiss = { showUpdateStatusDialog = false },
            onConfirmUpdate = { newStatus, notes, appointmentDate ->
                onUpdateStatus(activeApplication, newStatus, notes, appointmentDate)
                showUpdateStatusDialog = false
            }
        )
    }

    // ============================================================
    // 6. ADD CUSTOM DOCUMENT REQUIREMENT DIALOG
    // ============================================================
    if (showAddDocumentDialog) {
        AddDocumentRequirementDialog(
            onDismiss = { showAddDocumentDialog = false },
            onConfirmAdd = { title, desc ->
                onAddChecklistItem(activeApplication, title, desc)
                showAddDocumentDialog = false
            }
        )
    }

    // ============================================================
    // 7. NOTIFICATIONS HISTORY DIALOG
    // ============================================================
    if (showNotificationsHistoryDialog) {
        NotificationsHistoryDialog(
            notifications = inAppNotifications,
            onDismiss = { showNotificationsHistoryDialog = false }
        )
    }
}

/**
 * Dedicated Card highlighting the user's selected study program details
 */
@Composable
private fun SelectedStudyProgramCard(
    application: VisaApplication,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Flag, Country & Program Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Navy50,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = application.flagEmoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = application.country,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Text(
                            text = application.schoolName,
                            fontSize = 11.sp,
                            color = Slate600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Navy100
                ) {
                    Text(
                        text = application.visaType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy800,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Program Name Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Slate50,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Curso Seleccionado:",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = application.programName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Specs Grid (Duration, Start, Solvency, Processing)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Navy800, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Duración:", fontSize = 10.sp, color = Slate500)
                            }
                            Text(application.courseDuration, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Navy800, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inicio de Clases:", fontSize = 10.sp, color = Slate500)
                            }
                            Text(application.startDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = Emerald600, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solvencia requerida:", fontSize = 10.sp, color = Slate500)
                            }
                            Text(application.financialProofRequired, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Emerald700)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = Gold600, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tiempo estimado:", fontSize = 10.sp, color = Slate500)
                            }
                            Text(application.estimatedProcessingTime, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Representative for this Program
            OutlinedButton(
                onClick = onChatClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Navy800),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("chat_program_representative_button")
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, tint = Navy800, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Consultar al Asesor de ${application.schoolName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Quick Action Bar allowing users to mark document stages in 1 tap:
 * Application Sent, Appointment Scheduled, Approved, etc.
 */
@Composable
private fun QuickDocumentStageBar(
    currentStatus: VisaStatus,
    onSelectStage: (VisaStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avanzar Etapa de Documentos",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Navy50
                ) {
                    Text(
                        text = "☁️ Guarda en Firestore",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy800,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Application Sent
                StageActionChip(
                    title = "Application Sent",
                    subtitle = "Expediente Enviado",
                    icon = Icons.Default.Send,
                    isActive = currentStatus == VisaStatus.DOCUMENTOS_ENVIADOS,
                    accentColor = Navy800,
                    onClick = { onSelectStage(VisaStatus.DOCUMENTOS_ENVIADOS) },
                    modifier = Modifier.weight(1f)
                )

                // Appointment Scheduled
                StageActionChip(
                    title = "Appointment",
                    subtitle = "Cita Programada",
                    icon = Icons.Default.CalendarMonth,
                    isActive = currentStatus == VisaStatus.CITA_PROGRAMADA,
                    accentColor = Gold600,
                    onClick = { onSelectStage(VisaStatus.CITA_PROGRAMADA) },
                    modifier = Modifier.weight(1f)
                )

                // Approved
                StageActionChip(
                    title = "Approved",
                    subtitle = "¡Aprobada!",
                    icon = Icons.Default.Verified,
                    isActive = currentStatus == VisaStatus.APROBADO,
                    accentColor = Emerald600,
                    onClick = { onSelectStage(VisaStatus.APROBADO) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StageActionChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) accentColor.copy(alpha = 0.12f) else Slate50,
        border = BorderStroke(1.5.dp, if (isActive) accentColor else Slate200),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) accentColor else Slate600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) accentColor else Slate800,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Slate500,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Hero Card displaying active visa status and progress bar
 */
@Composable
private fun VisaApplicationHeroCard(
    application: VisaApplication,
    onUpdateStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = application.currentStatus
    val statusColor = when (status) {
        VisaStatus.APROBADO -> Emerald600
        VisaStatus.CITA_PROGRAMADA -> Gold600
        VisaStatus.DOCUMENTOS_ENVIADOS -> Navy700
        VisaStatus.EN_EVALUACION -> Cyan700
        VisaStatus.REQUIERE_SUBSANACION -> PeruRed
        else -> Slate700
    }
    val statusBg = when (status) {
        VisaStatus.APROBADO -> Emerald100
        VisaStatus.CITA_PROGRAMADA -> Gold100
        VisaStatus.DOCUMENTOS_ENVIADOS -> Navy100
        VisaStatus.EN_EVALUACION -> Cyan100
        VisaStatus.REQUIERE_SUBSANACION -> PeruRedLight
        else -> Slate100
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Flag, Country, Tracking ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Expediente Consular", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = application.trackingNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusBg,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (status) {
                            VisaStatus.APROBADO -> Icons.Default.Verified
                            VisaStatus.CITA_PROGRAMADA -> Icons.Default.Event
                            VisaStatus.DOCUMENTOS_ENVIADOS -> Icons.Default.CloudDone
                            VisaStatus.EN_EVALUACION -> Icons.Default.HourglassTop
                            VisaStatus.REQUIERE_SUBSANACION -> Icons.Default.Warning
                            else -> Icons.Default.FolderOpen
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estado actual: ",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                            Text(
                                text = status.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = statusColor
                            )
                        }
                        Text(
                            text = status.shortDesc,
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
            }

            // Progress Bar
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progreso del trámite en Firestore:",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Text(
                    text = "${(status.progressPercent * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Navy800
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { status.progressPercent },
                color = statusColor,
                trackColor = Slate200,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Appointment Date if available
            if (application.appointmentDate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Navy50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Próxima Cita Consular:", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text(application.appointmentDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy800)
                        }
                    }
                }
            }

            // Prominent Action Button: Actualizar Estado
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onUpdateStatusClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("update_visa_status_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar Etapa / Actualizar en Firestore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * Timeline Card illustrating the visa progression steps with interactive marking
 */
@Composable
private fun VisaJourneyTimelineCard(
    currentStatus: VisaStatus,
    onMarkStage: (VisaStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedSteps = listOf(
        VisaStatus.RECOPILANDO_DOCUMENTOS,
        VisaStatus.DOCUMENTOS_ENVIADOS,
        VisaStatus.BIOMETRICOS_PENDIENTES,
        VisaStatus.CITA_PROGRAMADA,
        VisaStatus.EN_EVALUACION,
        VisaStatus.APROBADO
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Etapas del Proceso Consular",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Toca para marcar",
                    fontSize = 10.sp,
                    color = Slate500,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            orderedSteps.forEachIndexed { index, step ->
                val isCompleted = currentStatus.stepOrder > step.stepOrder || currentStatus == VisaStatus.APROBADO
                val isCurrent = currentStatus == step

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMarkStage(step) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step Indicator Icon
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isCompleted -> Emerald600
                            isCurrent -> Navy800
                            else -> Slate200
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCurrent) PureWhite else Slate500
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = step.title,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = when {
                                    isCurrent -> Navy800
                                    isCompleted -> Emerald700
                                    else -> Slate600
                                }
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Navy100
                                ) {
                                    Text(
                                        text = "Fase Actual",
                                        color = Navy800,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = step.shortDesc,
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }

                    if (!isCurrent) {
                        OutlinedButton(
                            onClick = { onMarkStage(step) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Marcar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (index < orderedSteps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 13.dp)
                            .width(2.dp)
                            .height(14.dp)
                            .background(if (isCompleted) Emerald600 else Slate200)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Requirements Checklist Card synced with Firestore
 */
@Composable
private fun VisaRequirementsChecklistCard(
    checklist: List<VisaRequirementItem>,
    onToggle: (String) -> Unit,
    onAddDocumentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = checklist.count { it.isCompleted }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Checklist de Documentos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Marca cada documento para guardar en Firestore",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (completedCount == checklist.size) Emerald100 else Navy100
                ) {
                    Text(
                        text = "$completedCount / ${checklist.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedCount == checklist.size) Emerald700 else Navy800,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            checklist.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isCompleted) Emerald100.copy(alpha = 0.25f) else Slate50,
                    border = BorderStroke(1.dp, if (item.isCompleted) Emerald600.copy(alpha = 0.3f) else Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggle(item.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { onToggle(item.id) },
                            colors = CheckboxDefaults.colors(checkedColor = Emerald600)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (item.isCompleted) Slate800 else Slate700
                            )
                            Text(
                                text = item.description,
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }

                        if (item.isCompleted) {
                            Text(
                                text = "Listo ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddDocumentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar Documento al Checklist", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Historical log of status changes and notes stored in Firestore
 */
@Composable
private fun VisaHistoryLogCard(
    history: List<VisaStatusUpdate>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Registrado en Firestore",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Icon(Icons.Default.History, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            history.take(4).forEach { entry ->
                val dateStr = remember(entry.timestamp) {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate50,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.status.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Navy800
                            )
                            Text(dateStr, fontSize = 9.sp, color = Slate500)
                        }
                        if (entry.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = entry.notes, fontSize = 10.sp, color = Slate600)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Consular Office Card with Lima address & contacts
 */
@Composable
private fun ConsularOfficeCard(
    application: VisaApplication,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Oficina Consular en Lima, Perú 🇵🇪",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(application.embassyName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(application.embassyAddressInLima, fontSize = 11.sp, color = Slate600)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(application.officialHelpline, fontSize = 11.sp, color = Slate600)
            }
        }
    }
}

/**
 * Dialog to change visa status and sync to Firestore
 */
@Composable
private fun UpdateVisaStatusDialog(
    currentApplication: VisaApplication,
    initialStatus: VisaStatus = currentApplication.currentStatus,
    onDismiss: () -> Unit,
    onConfirmUpdate: (VisaStatus, String, String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(initialStatus) }
    var notes by remember { mutableStateOf("") }
    var appointmentDateInput by remember { mutableStateOf(currentApplication.appointmentDate ?: "") }

    val statusOptions = listOf(
        VisaStatus.DOCUMENTOS_ENVIADOS,
        VisaStatus.BIOMETRICOS_PENDIENTES,
        VisaStatus.CITA_PROGRAMADA,
        VisaStatus.EN_EVALUACION,
        VisaStatus.APROBADO,
        VisaStatus.REQUIERE_SUBSANACION
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Navy800)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar Etapa en Firestore", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Selecciona la etapa alcanzada para tu trámite de ${currentApplication.country}:",
                    fontSize = 12.sp,
                    color = Slate600
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(statusOptions) { status ->
                        val isSelected = selectedStatus == status
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Navy100 else Slate100,
                            border = BorderStroke(1.dp, if (isSelected) Navy800 else Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedStatus = status }
                                .testTag("status_option_${status.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedStatus = status },
                                    colors = RadioButtonDefaults.colors(selectedColor = Navy800)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = status.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Navy800 else Slate800
                                    )
                                    Text(
                                        text = status.shortDesc,
                                        fontSize = 10.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedStatus == VisaStatus.CITA_PROGRAMADA) {
                    OutlinedTextField(
                        value = appointmentDateInput,
                        onValueChange = { appointmentDateInput = it },
                        label = { Text("Fecha y Hora de la Cita") },
                        placeholder = { Text("Ej: 24 de Octubre, 2026 - 10:00 AM") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appointment_date_input")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas para Firestore (opcional)") },
                    placeholder = { Text("Ej: Confirmación recibida por correo del consulado.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val appt = if (selectedStatus == VisaStatus.CITA_PROGRAMADA && appointmentDateInput.isNotBlank()) {
                        appointmentDateInput
                    } else currentApplication.appointmentDate

                    onConfirmUpdate(selectedStatus, notes, appt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                modifier = Modifier.testTag("confirm_status_update_button")
            ) {
                Text("Guardar en Firestore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Dialog to add a custom document requirement to the checklist
 */
@Composable
private fun AddDocumentRequirementDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Navy800)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Documento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Añade un requisito adicional para dar seguimiento y almacenarlo en Firestore:",
                    fontSize = 12.sp,
                    color = Slate600
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del Documento") },
                    placeholder = { Text("Ej: Carta de antecedentes policiales") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detalle o Instrucción") },
                    placeholder = { Text("Ej: Legalizada ante el Ministerio de Relaciones Exteriores") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirmAdd(title, description)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy800)
            ) {
                Text("Guardar Documento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Dialog displaying history of real-time notifications
 */
@Composable
private fun NotificationsHistoryDialog(
    notifications: List<InAppNotification>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Navy800)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notificaciones (${notifications.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text("Aún no tienes notificaciones de cambios de estado.", color = Slate600, fontSize = 12.sp)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        val timeStr = remember(notif.timestamp) {
                            SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy800)
                                    Text(timeStr, fontSize = 9.sp, color = Slate500)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notif.message, fontSize = 11.sp, color = Slate700)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
