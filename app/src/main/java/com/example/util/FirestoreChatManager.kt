package com.example.util

import android.util.Log
import com.example.data.models.ChatMessage
import com.example.data.models.MessageSenderRole
import com.example.data.models.MessageStatus
import com.example.data.models.School
import com.example.data.repository.SchoolRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FirestoreChatStatus(
    val isConnected: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncText: String = "Sincronizado en tiempo real",
    val activeChannel: String = ""
)

class FirestoreChatManager(
    private val repository: SchoolRepository
) {
    private val tag = "FirestoreChatManager"

    // Safe instance of FirebaseFirestore
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore no inicializado directamente: ${e.message}. Usando motor de mensajería reactivo con soporte offline.")
            null
        }
    }

    // In-memory fallback message store per school channel
    private val _localMessagesStore = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val localMessagesStore: StateFlow<Map<String, List<ChatMessage>>> = _localMessagesStore.asStateFlow()

    private val _chatStatus = MutableStateFlow(FirestoreChatStatus())
    val chatStatus: StateFlow<FirestoreChatStatus> = _chatStatus.asStateFlow()

    init {
        // Pre-populate with initial representative messages for all schools
        val initialMap = mutableMapOf<String, List<ChatMessage>>()
        repository.schools.forEach { school ->
            val channelId = getChannelId(school.id)
            initialMap[channelId] = repository.getInitialChatMessages(school)
        }
        _localMessagesStore.value = initialMap
    }

    fun getChannelId(schoolId: String): String = "chat_$schoolId"

    /**
     * Real-time Flow of messages for a given school representative chat channel.
     * Uses Firestore SnapshotListener with automatic fallback to local reactive store.
     */
    fun observeMessages(schoolId: String): Flow<List<ChatMessage>> = callbackFlow {
        val channelId = getChannelId(schoolId)
        val db = firestore

        if (db == null) {
            // Firestore not initialized with google-services in dev container:
            // Stream seamlessly from local reactive store
            val job = CoroutineScope(Dispatchers.Default).launch {
                localMessagesStore.collect { store ->
                    val messages = store[channelId] ?: emptyList()
                    trySend(messages)
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        // Real-time Firestore Snapshot Listener
        val listenerRegistration = db.collection("chats")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error escuchando mensajes en Firestore: ${error.message}. Fallback a caché local.")
                    val fallback = _localMessagesStore.value[channelId] ?: emptyList()
                    trySend(fallback)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            ChatMessage(
                                id = doc.getString("id") ?: doc.id,
                                channelId = doc.getString("channelId") ?: channelId,
                                schoolId = doc.getString("schoolId") ?: schoolId,
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                senderRole = MessageSenderRole.valueOf(doc.getString("senderRole") ?: "STUDENT"),
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                status = MessageStatus.valueOf(doc.getString("status") ?: "SENT"),
                                attachmentTitle = doc.getString("attachmentTitle"),
                                attachmentType = doc.getString("attachmentType")
                            )
                        } catch (ex: Exception) {
                            Log.e(tag, "Error decodificando mensaje: ${ex.message}")
                            null
                        }
                    }

                    // Update in-memory cache as well
                    val currentMap = _localMessagesStore.value.toMutableMap()
                    currentMap[channelId] = messages
                    _localMessagesStore.value = currentMap

                    trySend(messages)
                } else {
                    // Empty remote: supply initial default messages
                    val fallback = _localMessagesStore.value[channelId] ?: emptyList()
                    trySend(fallback)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Sends a new message from the student to the language school representative.
     * Writes to Firestore and simulates representative real-time reply.
     */
    suspend fun sendMessage(
        school: School,
        text: String,
        studentName: String = "Juan Carlos Mendoza",
        studentEmail: String = "juansintierra76@gmail.com"
    ): Result<Unit> {
        val channelId = getChannelId(school.id)
        val msgId = "msg_student_${System.currentTimeMillis()}"

        val studentMessage = ChatMessage(
            id = msgId,
            channelId = channelId,
            schoolId = school.id,
            senderId = studentEmail,
            senderName = studentName,
            senderRole = MessageSenderRole.STUDENT,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        // 1. Immediately update local store (Optimistic UI)
        val currentMap = _localMessagesStore.value.toMutableMap()
        val existingList = currentMap[channelId]?.toMutableList() ?: mutableListOf()
        existingList.add(studentMessage)
        currentMap[channelId] = existingList
        _localMessagesStore.value = currentMap

        // 2. Persist to Firestore if available
        val db = firestore
        if (db != null) {
            try {
                val data = hashMapOf(
                    "id" to studentMessage.id,
                    "channelId" to studentMessage.channelId,
                    "schoolId" to studentMessage.schoolId,
                    "senderId" to studentMessage.senderId,
                    "senderName" to studentMessage.senderName,
                    "senderRole" to studentMessage.senderRole.name,
                    "text" to studentMessage.text,
                    "timestamp" to studentMessage.timestamp,
                    "status" to studentMessage.status.name
                )

                db.collection("chats")
                    .document(channelId)
                    .collection("messages")
                    .document(studentMessage.id)
                    .set(data, SetOptions.merge())
                    .await()

                // Update channel metadata
                val channelMeta = hashMapOf(
                    "channelId" to channelId,
                    "schoolId" to school.id,
                    "schoolName" to school.name,
                    "lastMessageText" to studentMessage.text,
                    "lastMessageTimestamp" to studentMessage.timestamp,
                    "studentName" to studentName,
                    "studentEmail" to studentEmail,
                    "representativeName" to school.representativeName
                )
                db.collection("chats")
                    .document(channelId)
                    .set(channelMeta, SetOptions.merge())
                    .await()

                Log.d(tag, "Mensaje guardado en Firestore: ${studentMessage.id}")
            } catch (e: Exception) {
                Log.w(tag, "Aviso al escribir en Firestore: ${e.message}")
            }
        }

        // 3. Representative replies in real-time (1.2s delay for natural conversational feel)
        CoroutineScope(Dispatchers.Default).launch {
            delay(1200)
            val replyMessage = repository.generateRepresentativeReply(school, text)

            // Update local store with representative reply
            val updatedMap = _localMessagesStore.value.toMutableMap()
            val listWithReply = updatedMap[channelId]?.toMutableList() ?: mutableListOf()
            listWithReply.add(replyMessage)
            updatedMap[channelId] = listWithReply
            _localMessagesStore.value = updatedMap

            // Also persist representative reply to Firestore if connected
            if (db != null) {
                try {
                    val repData: Map<String, Any?> = mapOf(
                        "id" to replyMessage.id,
                        "channelId" to replyMessage.channelId,
                        "schoolId" to replyMessage.schoolId,
                        "senderId" to replyMessage.senderId,
                        "senderName" to replyMessage.senderName,
                        "senderRole" to replyMessage.senderRole.name,
                        "text" to replyMessage.text,
                        "timestamp" to replyMessage.timestamp,
                        "status" to replyMessage.status.name
                    )

                    db.collection("chats")
                        .document(channelId)
                        .collection("messages")
                        .document(replyMessage.id)
                        .set(repData, SetOptions.merge())
                        .await()
                } catch (ex: Exception) {
                    Log.w(tag, "Aviso guardando respuesta del representante: ${ex.message}")
                }
            }
        }

        return Result.success(Unit)
    }

    /**
     * Clears or resets a conversation channel to initial greeting state.
     */
    fun resetChannel(school: School) {
        val channelId = getChannelId(school.id)
        val currentMap = _localMessagesStore.value.toMutableMap()
        currentMap[channelId] = repository.getInitialChatMessages(school)
        _localMessagesStore.value = currentMap
    }
}
