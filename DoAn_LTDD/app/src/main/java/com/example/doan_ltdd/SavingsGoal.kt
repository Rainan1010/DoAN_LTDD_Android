package com.example.doan_ltdd

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var targetAmount: Double,
    var currentAmount: Double = 0.0,
    var category: String,
    var priority: String,
    var deadline: LocalDateTime,

    // --- CÁC TRƯỜNG CHO THÔNG BÁO (NOTIFICATION) ---
    var isReminderEnabled: Boolean = false,
    var reminderFrequency: String = "DAILY", // "DAILY" hoặc "MONTHLY"
    var reminderHour: Int = 9,      // Giờ mặc định (ví dụ 9h sáng)
    var reminderMinute: Int = 0,    // Phút mặc định
    var reminderDayOfMonth: Int = 1,// Ngày trong tháng (cho chế độ Monthly)

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var iconResId: Int = R.drawable.ic_home // Icon của bạn
) {
    // ... (Giữ nguyên các hàm progressPercentage, isDeadlineApproaching, addDeposit) ...
    val progressPercentage: Int
        get() {
            if (targetAmount <= 0) return 0
            return ((currentAmount / targetAmount) * 100).toInt()
        }

    fun isDeadlineApproaching(daysWarning: Int = 3): Boolean {
        val now = LocalDateTime.now()
        if (now.isAfter(deadline)) return false
        val daysLeft = ChronoUnit.DAYS.between(now, deadline)
        return daysLeft <= daysWarning
    }

    fun addDeposit(amount: Double) {
        this.currentAmount += amount
    }
}