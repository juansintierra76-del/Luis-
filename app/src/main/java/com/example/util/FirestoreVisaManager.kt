package com.example.util

import android.util.Log
import com.example.data.models.VisaApplication
import com.example.data.models.VisaRequirementItem
import com.example.data.models.VisaStatus
import com.example.data.models.VisaStatusUpdate
import com.example.data.repository.VisaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class VisaSyncStatus(
    val isConnected: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String = "Sincronizado con Cloud Firestore",
    val lastSyncTime: Long = System.currentTimeMillis()
)

class FirestoreVisaManager(
    private val visaRepository: VisaRepository = VisaRepository()
) {
    private val tag = "FirestoreVisaManager"
    private val collectionName = "visa_applications"
    private val scope = CoroutineScope(Dispatchers.IO)

    // Safe instance of FirebaseFirestore
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore no inicializado directamente: ${e.message}. Usando almacenamiento en memoria con soporte offline.")
            null
        }
    }

    // In-memory fallback / current state
    private val _localApplications = MutableStateFlow(visaRepository.getInitialApplications())
    val localApplications: StateFlow<List<VisaApplication>> = _localApplications.asStateFlow()

    private val _syncStatus = MutableStateFlow(VisaSyncStatus())
    val syncStatus: StateFlow<VisaSyncStatus> = _syncStatus.asStateFlow()

    init {
        // Seed initial documents to Firestore if connected
        scope.launch {
            seedInitialApplicationsIfNeeded()
        }
    }

    private suspend fun seedInitialApplicationsIfNeeded() {
        val db = firestore ?: return
        try {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncMessage = "Verificando Cloud Firestore...")
            val snapshot = db.collection(collectionName).get().await()
            if (snapshot.isEmpty) {
                Log.d(tag, "Colección de visas vacía. Sembrando trámites iniciales...")
                val initialList = visaRepository.getInitialApplications()
                for (app in initialList) {
                    val map = appToMap(app)
                    db.collection(collectionName).document(app.id).set(map, SetOptions.merge()).await()
                }
            }
            _syncStatus.value = VisaSyncStatus(
                isConnected = true,
                isSyncing = false,
                lastSyncMessage = "Sincronizado con Cloud Firestore",
                lastSyncTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(tag, "Error al sembrar datos en Firestore: ${e.message}", e)
            _syncStatus.value = VisaSyncStatus(
                isConnected = false,
                isSyncing = false,
                lastSyncMessage = "Modo Local / Offline (Firestore pendiente de red)",
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Observes visa applications in real-time from Firestore.
     * Falls back to local in-memory store if Firestore is unavailable.
     */
    fun observeVisaApplications(): Flow<List<VisaApplication>> = callbackFlow {
        val db = firestore
        if (db == null) {
            // Emite estado local y cierra el canal
            val job = launch {
                _localApplications.collect { apps ->
                    trySend(apps)
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        val registration = db.collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error en listener de Firestore: ${error.message}. Usando estado local.")
                    _syncStatus.value = _syncStatus.value.copy(
                        isConnected = false,
                        lastSyncMessage = "Firestore offline (datos locales conservados)"
                    )
                    trySend(_localApplications.value)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val apps = snapshot.documents.mapNotNull { doc ->
                        docToVisaApplication(doc.id, doc.data ?: emptyMap())
                    }
                    if (apps.isNotEmpty()) {
                        _localApplications.value = apps
                        _syncStatus.value = VisaSyncStatus(
                            isConnected = true,
                            isSyncing = false,
                            lastSyncMessage = "Sincronizado con Cloud Firestore",
                            lastSyncTime = System.currentTimeMillis()
                        )
                        trySend(apps)
                        return@addSnapshotListener
                    }
                }

                // If Firestore collection returned empty or null, seed and emit local
                trySend(_localApplications.value)
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Updates the status/stage of a visa application and saves to Firestore.
     */
    fun updateVisaStatus(
        applicationId: String,
        newStatus: VisaStatus,
        notes: String,
        appointmentDate: String?
    ) {
        val currentList = _localApplications.value
        val app = currentList.find { it.id == applicationId } ?: return

        val newUpdate = VisaStatusUpdate(
            id = "update_${System.currentTimeMillis()}",
            status = newStatus,
            timestamp = System.currentTimeMillis(),
            notes = notes.ifBlank { "Etapa actualizada a ${newStatus.title}" }
        )

        val updatedApp = app.copy(
            currentStatus = newStatus,
            appointmentDate = appointmentDate ?: app.appointmentDate,
            lastUpdated = System.currentTimeMillis(),
            history = listOf(newUpdate) + app.history
        )

        // Optimistic local update
        _localApplications.value = currentList.map { if (it.id == applicationId) updatedApp else it }

        // Sync with Firestore
        scope.launch {
            val db = firestore
            if (db != null) {
                try {
                    _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncMessage = "Guardando en Firestore...")
                    val map = appToMap(updatedApp)
                    db.collection(collectionName).document(applicationId).set(map, SetOptions.merge()).await()
                    _syncStatus.value = VisaSyncStatus(
                        isConnected = true,
                        isSyncing = false,
                        lastSyncMessage = "Etapa guardada en Cloud Firestore",
                        lastSyncTime = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Error guardando etapa en Firestore: ${e.message}", e)
                    _syncStatus.value = _syncStatus.value.copy(
                        isConnected = false,
                        isSyncing = false,
                        lastSyncMessage = "Guardado localmente (pendiente de sincronización)"
                    )
                }
            }
        }
    }

    /**
     * Toggles a checklist item and saves the updated checklist to Firestore.
     */
    fun toggleChecklistItem(applicationId: String, itemId: String) {
        val currentList = _localApplications.value
        val app = currentList.find { it.id == applicationId } ?: return

        val updatedChecklist = app.checklist.map { item ->
            if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
        }
        val updatedApp = app.copy(
            checklist = updatedChecklist,
            lastUpdated = System.currentTimeMillis()
        )

        // Optimistic local update
        _localApplications.value = currentList.map { if (it.id == applicationId) updatedApp else it }

        // Sync with Firestore
        scope.launch {
            val db = firestore
            if (db != null) {
                try {
                    _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncMessage = "Actualizando checklist en Firestore...")
                    val checklistMap = updatedChecklist.map { item ->
                        mapOf(
                            "id" to item.id,
                            "title" to item.title,
                            "description" to item.description,
                            "isCompleted" to item.isCompleted,
                            "isMandatory" to item.isMandatory
                        )
                    }
                    db.collection(collectionName).document(applicationId).update(
                        mapOf(
                            "checklist" to checklistMap,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    ).await()
                    _syncStatus.value = VisaSyncStatus(
                        isConnected = true,
                        isSyncing = false,
                        lastSyncMessage = "Checklist sincronizado en Firestore",
                        lastSyncTime = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Error actualizando checklist en Firestore: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Adds a new custom requirement/stage to the checklist and persists in Firestore.
     */
    fun addChecklistItem(applicationId: String, title: String, description: String) {
        val currentList = _localApplications.value
        val app = currentList.find { it.id == applicationId } ?: return

        val newItem = VisaRequirementItem(
            id = "chk_${System.currentTimeMillis()}",
            title = title,
            description = description,
            isCompleted = false,
            isMandatory = false
        )
        val updatedChecklist = app.checklist + newItem
        val updatedApp = app.copy(
            checklist = updatedChecklist,
            lastUpdated = System.currentTimeMillis()
        )

        _localApplications.value = currentList.map { if (it.id == applicationId) updatedApp else it }

        scope.launch {
            val db = firestore
            if (db != null) {
                try {
                    val map = appToMap(updatedApp)
                    db.collection(collectionName).document(applicationId).set(map, SetOptions.merge()).await()
                } catch (e: Exception) {
                    Log.e(tag, "Error agregando item en Firestore: ${e.message}")
                }
            }
        }
    }

    /**
     * Force re-sync with Firestore
     */
    fun refreshFromFirestore() {
        scope.launch {
            val db = firestore ?: return@launch
            try {
                _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastSyncMessage = "Sincronizando con Cloud Firestore...")
                val snapshot = db.collection(collectionName).get().await()
                val apps = snapshot.documents.mapNotNull { doc ->
                    docToVisaApplication(doc.id, doc.data ?: emptyMap())
                }
                if (apps.isNotEmpty()) {
                    _localApplications.value = apps
                }
                _syncStatus.value = VisaSyncStatus(
                    isConnected = true,
                    isSyncing = false,
                    lastSyncMessage = "Sincronizado con Cloud Firestore",
                    lastSyncTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.e(tag, "Error al refrescar de Firestore: ${e.message}")
                _syncStatus.value = VisaSyncStatus(
                    isConnected = false,
                    isSyncing = false,
                    lastSyncMessage = "Modo Local / Error de conexión",
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        }
    }

    // Helper: Map VisaApplication to Firestore Map
    private fun appToMap(app: VisaApplication): Map<String, Any?> {
        return mapOf(
            "id" to app.id,
            "schoolId" to app.schoolId,
            "programName" to app.programName,
            "courseDuration" to app.courseDuration,
            "startDate" to app.startDate,
            "financialProofRequired" to app.financialProofRequired,
            "estimatedProcessingTime" to app.estimatedProcessingTime,
            "country" to app.country,
            "countryCode" to app.countryCode,
            "flagEmoji" to app.flagEmoji,
            "visaType" to app.visaType,
            "schoolName" to app.schoolName,
            "trackingNumber" to app.trackingNumber,
            "currentStatus" to app.currentStatus.name,
            "appointmentDate" to (app.appointmentDate ?: ""),
            "appointmentLocation" to (app.appointmentLocation ?: ""),
            "embassyName" to app.embassyName,
            "embassyAddressInLima" to app.embassyAddressInLima,
            "officialHelpline" to app.officialHelpline,
            "lastUpdated" to app.lastUpdated,
            "checklist" to app.checklist.map { item ->
                mapOf(
                    "id" to item.id,
                    "title" to item.title,
                    "description" to item.description,
                    "isCompleted" to item.isCompleted,
                    "isMandatory" to item.isMandatory
                )
            },
            "history" to app.history.map { h ->
                mapOf(
                    "id" to h.id,
                    "status" to h.status.name,
                    "timestamp" to h.timestamp,
                    "notes" to h.notes,
                    "updatedBy" to h.updatedBy
                )
            }
        )
    }

    // Helper: Map Firestore Map to VisaApplication
    @Suppress("UNCHECKED_CAST")
    private fun docToVisaApplication(id: String, data: Map<String, Any>): VisaApplication? {
        return try {
            val statusString = data["currentStatus"] as? String ?: VisaStatus.RECOPILANDO_DOCUMENTOS.name
            val status = try {
                VisaStatus.valueOf(statusString)
            } catch (e: Exception) {
                VisaStatus.RECOPILANDO_DOCUMENTOS
            }

            val checklistRaw = data["checklist"] as? List<Map<String, Any>> ?: emptyList()
            val checklist = checklistRaw.map { itemMap ->
                VisaRequirementItem(
                    id = itemMap["id"] as? String ?: "chk_${System.currentTimeMillis()}",
                    title = itemMap["title"] as? String ?: "",
                    description = itemMap["description"] as? String ?: "",
                    isCompleted = itemMap["isCompleted"] as? Boolean ?: false,
                    isMandatory = itemMap["isMandatory"] as? Boolean ?: true
                )
            }

            val historyRaw = data["history"] as? List<Map<String, Any>> ?: emptyList()
            val history = historyRaw.map { hMap ->
                val hStatusStr = hMap["status"] as? String ?: VisaStatus.RECOPILANDO_DOCUMENTOS.name
                val hStatus = try {
                    VisaStatus.valueOf(hStatusStr)
                } catch (e: Exception) {
                    VisaStatus.RECOPILANDO_DOCUMENTOS
                }
                VisaStatusUpdate(
                    id = hMap["id"] as? String ?: "hist_${System.currentTimeMillis()}",
                    status = hStatus,
                    timestamp = (hMap["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    notes = hMap["notes"] as? String ?: "",
                    updatedBy = hMap["updatedBy"] as? String ?: "Estudiante"
                )
            }

            val appointmentDate = data["appointmentDate"] as? String
            val appointmentLocation = data["appointmentLocation"] as? String

            VisaApplication(
                id = id,
                country = data["country"] as? String ?: "Destino",
                countryCode = data["countryCode"] as? String ?: "PE",
                flagEmoji = data["flagEmoji"] as? String ?: "✈️",
                visaType = data["visaType"] as? String ?: "Student Visa",
                schoolName = data["schoolName"] as? String ?: "Escuela de Idiomas",
                trackingNumber = data["trackingNumber"] as? String ?: "TRK-${System.currentTimeMillis()}",
                currentStatus = status,
                appointmentDate = if (appointmentDate.isNullOrBlank()) null else appointmentDate,
                appointmentLocation = if (appointmentLocation.isNullOrBlank()) null else appointmentLocation,
                embassyName = data["embassyName"] as? String ?: "Embajada en Lima",
                embassyAddressInLima = data["embassyAddressInLima"] as? String ?: "Lima, Perú",
                officialHelpline = data["officialHelpline"] as? String ?: "",
                lastUpdated = (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                checklist = checklist,
                history = history,
                schoolId = data["schoolId"] as? String ?: "",
                programName = data["programName"] as? String ?: "General English Intensive",
                courseDuration = data["courseDuration"] as? String ?: "24 Semanas (6 Meses)",
                startDate = data["startDate"] as? String ?: "02 de Febrero, 2026",
                financialProofRequired = data["financialProofRequired"] as? String ?: "Demostración de fondos oficiales",
                estimatedProcessingTime = data["estimatedProcessingTime"] as? String ?: "4 a 6 semanas"
            )
        } catch (e: Exception) {
            Log.e(tag, "Error convirtiendo documento a VisaApplication: ${e.message}", e)
            null
        }
    }
}
