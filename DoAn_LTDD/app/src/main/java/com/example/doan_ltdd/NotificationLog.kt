package com.example.doan_ltdd

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false // Trạng thái đã xem (để tô đậm nếu chưa xem)
)