package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.SchoolRepository
import com.example.data.repository.VisaRepository
import com.example.ui.components.NavigationTab
import com.example.util.FirestoreChatManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    val repository: SchoolRepository = SchoolRepository(),
    val firestoreChatManager: FirestoreChatManager = FirestoreChatManager(repository),
    val visaRepository: VisaRepository = VisaRepository(),
    val firestoreVisaManager: com.example.util.FirestoreVisaManager = com.example.util.FirestoreVisaManager(visaRepository)
) : ViewModel() {

    val schools: List<School> = repository.schools

    private val _currentTab = MutableStateFlow(NavigationTab.CATALOG)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _activeChatSchool = MutableStateFlow(repository.schools.first())
    val activeChatSchool: StateFlow<School> = _activeChatSchool.asStateFlow()

    private val _currencyMode = MutableStateFlow(CurrencyMode.USD)
    val currencyMode: StateFlow<CurrencyMode> = _currencyMode.asStateFlow()

    private val _bookmarkedSchoolIds = MutableStateFlow(setOf("ilsc_melbourne", "ilac_toronto"))
    val bookmarkedSchoolIds: StateFlow<Set<String>> = _bookmarkedSchoolIds.asStateFlow()

    private val _isSavedSchoolsDialogOpen = MutableStateFlow(false)
    val isSavedSchoolsDialogOpen: StateFlow<Boolean> = _isSavedSchoolsDialogOpen.asStateFlow()

    private val _preEnrollSchool = MutableStateFlow<School>(repository.schools.first())
    val preEnrollSchool: StateFlow<School> = _preEnrollSchool.asStateFlow()

    // Real-time Chat Messages flow that re-subscribes whenever activeChatSchool changes
    val chatMessages: StateFlow<List<ChatMessage>> = _activeChatSchool
        .flatMapLatest { school ->
            firestoreChatManager.observeMessages(school.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getInitialChatMessages(repository.schools.first())
        )

    // ============================================================
    // VISA TRACKER STATE & NOTIFICATIONS WITH FIRESTORE
    // ============================================================
    val visaApplications: StateFlow<List<VisaApplication>> = firestoreVisaManager.observeVisaApplications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = visaRepository.getInitialApplications()
        )

    val visaSyncStatus: StateFlow<com.example.util.VisaSyncStatus> = firestoreVisaManager.syncStatus

    private val _selectedVisaApplicationId = MutableStateFlow(visaRepository.getInitialApplications().first().id)

    val activeVisaApplication: StateFlow<VisaApplication> = combine(
        visaApplications,
        _selectedVisaApplicationId
    ) { apps, selectedId ->
        apps.find { it.id == selectedId } ?: apps.firstOrNull() ?: visaRepository.getInitialApplications().first()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = visaRepository.getInitialApplications().first()
    )

    private val _inAppNotifications = MutableStateFlow<List<InAppNotification>>(
        listOf(
            InAppNotification(
                id = "init_notif_1",
                title = "🛂 Expediente Enviado",
                message = "Tu expediente para Australia (Subclass 500) fue ingresado con éxito en el sistema consular.",
                timestamp = System.currentTimeMillis() - 7200000,
                country = "Australia"
            )
        )
    )
    val inAppNotifications: StateFlow<List<InAppNotification>> = _inAppNotifications.asStateFlow()

    private val _latestNotification = MutableStateFlow<InAppNotification?>(null)
    val latestNotification: StateFlow<InAppNotification?> = _latestNotification.asStateFlow()

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectChatSchool(school: School) {
        _activeChatSchool.value = school
        _currentTab.value = NavigationTab.CHATS
    }

    fun startPreEnrollment(school: School) {
        _preEnrollSchool.value = school
        _currentTab.value = NavigationTab.APPLICATIONS
    }

    fun submitPreEnrollment(
        school: School,
        weeks: Int,
        studentName: String,
        studentEmail: String,
        studentPhone: String,
        studentRegion: String,
        program: String
    ) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val data = mapOf(
                    "schoolId" to school.id,
                    "schoolName" to school.name,
                    "country" to school.country,
                    "weeks" to weeks,
                    "studentName" to studentName,
                    "studentEmail" to studentEmail,
                    "studentPhone" to studentPhone,
                    "studentRegion" to studentRegion,
                    "program" to program,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("pre_enrollments")
                    .document("enroll_${school.id}_${System.currentTimeMillis()}")
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Aviso guardando prematrícula en Firestore: ${e.message}")
            }
        }
    }

    fun toggleCurrency() {
        _currencyMode.value = if (_currencyMode.value == CurrencyMode.USD) CurrencyMode.PEN else CurrencyMode.USD
    }

    fun toggleBookmark(school: School) {
        val current = _bookmarkedSchoolIds.value.toMutableSet()
        if (current.contains(school.id)) {
            current.remove(school.id)
        } else {
            current.add(school.id)
        }
        _bookmarkedSchoolIds.value = current
    }

    fun openSavedSchools() {
        _isSavedSchoolsDialogOpen.value = true
    }

    fun closeSavedSchools() {
        _isSavedSchoolsDialogOpen.value = false
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentSchool = _activeChatSchool.value
        viewModelScope.launch {
            firestoreChatManager.sendMessage(
                school = currentSchool,
                text = text
            )
        }
    }

    // ============================================================
    // VISA TRACKER METHODS WITH FIRESTORE
    // ============================================================
    fun selectVisaApplication(application: VisaApplication) {
        _selectedVisaApplicationId.value = application.id
    }

    fun updateVisaStatus(
        context: Context,
        application: VisaApplication,
        newStatus: VisaStatus,
        notes: String,
        appointmentDate: String?
    ) {
        // Persist to Cloud Firestore
        firestoreVisaManager.updateVisaStatus(
            applicationId = application.id,
            newStatus = newStatus,
            notes = notes,
            appointmentDate = appointmentDate
        )

        // 1. Send Android Push / System Notification
        NotificationHelper.sendVisaStatusNotification(
            context = context,
            countryName = application.country,
            newStatusTitle = newStatus.title,
            statusDescription = newStatus.shortDesc,
            trackingNumber = application.trackingNumber
        )

        // 2. Add In-App Real-Time Notification
        val notifTitle = when (newStatus) {
            VisaStatus.APROBADO -> "🎉 ¡Visa Aprobada para ${application.country}!"
            VisaStatus.CITA_PROGRAMADA -> "📅 Cita Consular Programada: ${application.country}"
            VisaStatus.DOCUMENTOS_ENVIADOS -> "📤 Documentos Enviados: ${application.country}"
            VisaStatus.EN_EVALUACION -> "⏳ En Evaluación Consular: ${application.country}"
            VisaStatus.BIOMETRICOS_PENDIENTES -> "📸 Biométricos Pendientes: ${application.country}"
            else -> "🛂 Actualización de Visa: ${application.country}"
        }

        val notifMessage = if (newStatus == VisaStatus.CITA_PROGRAMADA && appointmentDate != null) {
            "Tu cita consular ha sido fijada para: $appointmentDate en ${application.appointmentLocation ?: "el consulado"}."
        } else {
            "Tu trámite de visa para ${application.schoolName} ha pasado a: '${newStatus.title}'. ${newStatus.shortDesc}"
        }

        val inAppNotif = InAppNotification(
            id = "notif_${System.currentTimeMillis()}",
            title = notifTitle,
            message = notifMessage,
            timestamp = System.currentTimeMillis(),
            country = application.country
        )

        _inAppNotifications.value = listOf(inAppNotif) + _inAppNotifications.value
        _latestNotification.value = inAppNotif
    }

    fun toggleChecklistItem(application: VisaApplication, itemId: String) {
        firestoreVisaManager.toggleChecklistItem(application.id, itemId)
    }

    fun addCustomChecklistItem(application: VisaApplication, title: String, description: String) {
        firestoreVisaManager.addChecklistItem(application.id, title, description)
    }

    fun refreshVisaFromFirestore() {
        firestoreVisaManager.refreshFromFirestore()
    }

    fun dismissLatestNotification() {
        _latestNotification.value = null
    }
}
