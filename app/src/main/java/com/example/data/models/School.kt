package com.example.data.models

data class School(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val flagEmoji: String,
    val accreditation: String,
    val rating: Double,
    val reviewsCount: Int,
    val priceWeeklyUsd: Double,
    val representativeName: String,
    val representativeRole: String,
    val representativeOnline: Boolean = true,
    val representativeTimezone: String = "UTC+10",
    val popularPrograms: List<String> = listOf("General English", "IELTS Preparation", "Business English"),
    val visaAllowsWork: Boolean = false,
    val workDetails: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

enum class MessageSenderRole {
    STUDENT,
    REPRESENTATIVE,
    COUNSELOR
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class ChatMessage(
    val id: String = "",
    val channelId: String = "",
    val schoolId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: MessageSenderRole = MessageSenderRole.STUDENT,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val attachmentTitle: String? = null,
    val attachmentType: String? = null
)
