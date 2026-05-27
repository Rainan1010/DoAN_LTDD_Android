package com.example.doan_ltdd

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "deposit_logs")
data class DepositLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val goalId: String, // Khóa ngoại (logical) tham chiếu tới SavingsGoal
    val goalName: String,
    val amount: Double,
    val transactionDate: LocalDateTime = LocalDateTime.now()
)