package com.example.doan_ltdd

import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

object SampleDataProvider {

    // Danh sách danh mục mẫu để MainActivity có thể dùng khởi tạo DB
    fun getAllCategories(): List<String> {
        return listOf(
            "Mua sắm", "Du lịch", "Xe cộ", "Nhà cửa", "Tiết kiệm chung",
            "Công nghệ", "Sức khỏe", "Giáo dục", "Giải trí", "Đầu tư", "Khẩn cấp"
        )
    }

    fun getSavingsGoals(): List<SavingsGoal> {
        return listOf(
            // --- CÁC MỤC TIÊU CỤ THỂ ---
            SavingsGoal(
                id = "8f39c2a1-b4d2-4e1f-9a3b-2c1e5f6a8b9d",
                name = "Mua iPhone 15",
                targetAmount = 30000000.0,
                currentAmount = 10000000.0,
                category = "Công nghệ",
                priority = "Cao",
                deadline = LocalDateTime.parse("2024-05-20T10:00:00"),
                isReminderEnabled = true,
                reminderFrequency = "DAILY",
                reminderHour = 9,
                reminderMinute = 0,
                createdAt = LocalDateTime.parse("2023-11-20T08:00:00"),
                iconResId = R.drawable.ic_mobile
            ),
            SavingsGoal(
                id = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                name = "Du lịch Nhật Bản",
                targetAmount = 50000000.0,
                currentAmount = 12000000.0,
                category = "Du lịch",
                priority = "Trung bình",
                deadline = LocalDateTime.parse("2024-12-01T00:00:00"),
                isReminderEnabled = true,
                reminderFrequency = "MONTHLY",
                reminderDayOfMonth = 5,
                createdAt = LocalDateTime.parse("2023-01-15T14:30:00"),
                iconResId = R.drawable.ic_travel
            ),
            SavingsGoal(
                id = "b2c3d4e5-f6a7-8901-2345-678901bcdef0",
                name = "Quỹ khẩn cấp",
                targetAmount = 100000000.0,
                currentAmount = 45000000.0,
                category = "Khẩn cấp",
                priority = "Cao",
                deadline = LocalDateTime.parse("2025-01-01T00:00:00"),
                isReminderEnabled = false,
                createdAt = LocalDateTime.parse("2022-06-10T09:00:00"),
                iconResId = R.drawable.ic_health
            ),
            // --- CÁC MỤC TIÊU BỔ SUNG (Đã Việt hóa) ---
            SavingsGoal(
                id = "c3d4e5f6-a7b8-9012-3456-789012cdef01",
                name = "Mua ô tô VinFast",
                targetAmount = 500000000.0,
                currentAmount = 150000000.0,
                category = "Xe cộ",
                priority = "Cao",
                deadline = LocalDateTime.parse("2026-06-15T12:00:00"),
                createdAt = LocalDateTime.parse("2023-03-20T10:15:00"),
                iconResId = R.drawable.ic_car
            ),
            SavingsGoal(
                id = "d4e5f6a7-b8c9-0123-4567-890123def012", name = "Quà cưới bạn thân", targetAmount = 5000000.0, currentAmount = 2000000.0, category = "Mua sắm", priority = "Trung bình", deadline = LocalDateTime.parse("2024-02-14T18:00:00"), createdAt = LocalDateTime.parse("2023-12-01T08:00:00")
            ),
            SavingsGoal(
                id = "e5f6a7b8-c9d0-1234-5678-901234ef0123", name = "Macbook Pro M3", targetAmount = 45000000.0, currentAmount = 10000000.0, category = "Công nghệ", priority = "Cao", deadline = LocalDateTime.parse("2024-09-10T09:00:00"), createdAt = LocalDateTime.parse("2023-10-05T11:20:00")
            ),
            SavingsGoal(
                id = "f6a7b8c9-d0e1-2345-6789-012345f01234", name = "Tiền cọc mua nhà", targetAmount = 1000000000.0, currentAmount = 200000000.0, category = "Nhà cửa", priority = "Cao", deadline = LocalDateTime.parse("2030-01-01T00:00:00"), createdAt = LocalDateTime.parse("2021-05-15T15:00:00"), iconResId = R.drawable.ic_home
            ),
            SavingsGoal(
                id = "a7b8c9d0-e1f2-3456-7890-123456012345", name = "Vé Concert", targetAmount = 3000000.0, currentAmount = 3000000.0, category = "Giải trí", priority = "Thấp", deadline = LocalDateTime.parse("2024-03-25T20:00:00"), createdAt = LocalDateTime.parse("2023-12-20T10:00:00")
            ),
            SavingsGoal(
                id = "b8c9d0e1-f2a3-4567-8901-234567123456", name = "Sửa chữa nhà bếp", targetAmount = 50000000.0, currentAmount = 5000000.0, category = "Nhà cửa", priority = "Trung bình", deadline = LocalDateTime.parse("2024-08-01T08:00:00"), createdAt = LocalDateTime.parse("2023-09-01T09:30:00"), iconResId = R.drawable.ic_home
            ),
            SavingsGoal(
                id = "c9d0e1f2-a3b4-5678-9012-345678234567", name = "Khóa học Tiếng Anh", targetAmount = 12000000.0, currentAmount = 6000000.0, category = "Giáo dục", priority = "Cao", deadline = LocalDateTime.parse("2024-04-30T23:59:59"), createdAt = LocalDateTime.parse("2024-01-01T12:00:00")
            ),
            SavingsGoal(id = "d0e1f2a3-b4c5-6789-0123-456789345678", name = "Ủng hộ từ thiện", targetAmount = 2000000.0, currentAmount = 500000.0, category = "Mua sắm", priority = "Thấp", deadline = LocalDateTime.parse("2024-12-25T00:00:00"), createdAt = LocalDateTime.parse("2023-11-15T14:00:00")),
            SavingsGoal(id = "e1f2a3b4-c5d6-7890-1234-567890456789", name = "Khám sức khỏe tổng quát", targetAmount = 5000000.0, currentAmount = 1000000.0, category = "Sức khỏe", priority = "Cao", deadline = LocalDateTime.parse("2024-06-01T08:00:00"), createdAt = LocalDateTime.parse("2024-01-10T10:00:00"), iconResId = R.drawable.ic_health),
            SavingsGoal(id = "f2a3b4c5-d6e7-8901-2345-678901567890", name = "PC Gaming mới", targetAmount = 40000000.0, currentAmount = 15000000.0, category = "Công nghệ", priority = "Trung bình", deadline = LocalDateTime.parse("2024-11-11T00:00:00"), createdAt = LocalDateTime.parse("2023-08-20T16:00:00")),
            SavingsGoal(id = "a3b4c5d6-e7f8-9012-3456-789012678901", name = "Du lịch hè Đà Nẵng", targetAmount = 15000000.0, currentAmount = 0.0, category = "Du lịch", priority = "Trung bình", deadline = LocalDateTime.parse("2024-07-01T06:00:00"), createdAt = LocalDateTime.parse("2024-01-20T09:00:00"), iconResId = R.drawable.ic_travel),
            SavingsGoal(id = "b4c5d6e7-f8a9-0123-4567-890123789012", name = "Mua tủ quần áo", targetAmount = 10000000.0, currentAmount = 2000000.0, category = "Mua sắm", priority = "Thấp", deadline = LocalDateTime.parse("2024-05-01T10:00:00"), createdAt = LocalDateTime.parse("2024-01-05T13:00:00")),
            SavingsGoal(id = "c5d6e7f8-a9b0-1234-5678-901234890123", name = "Đầu tư chứng khoán", targetAmount = 50000000.0, currentAmount = 30000000.0, category = "Đầu tư", priority = "Cao", deadline = LocalDateTime.parse("2024-12-31T15:00:00"), createdAt = LocalDateTime.parse("2023-07-01T09:00:00")),
            SavingsGoal(id = "d6e7f8a9-b0c1-2345-6789-012345901234", name = "Nâng cấp xe máy", targetAmount = 8000000.0, currentAmount = 4000000.0, category = "Xe cộ", priority = "Trung bình", deadline = LocalDateTime.parse("2024-04-15T17:00:00"), createdAt = LocalDateTime.parse("2023-11-01T11:00:00"), iconResId = R.drawable.ic_motorcycle),
            SavingsGoal(id = "e7f8a9b0-c1d2-3456-7890-123456012345", name = "Thi chứng chỉ IELTS", targetAmount = 4500000.0, currentAmount = 4500000.0, category = "Giáo dục", priority = "Cao", deadline = LocalDateTime.parse("2024-03-10T08:00:00"), createdAt = LocalDateTime.parse("2023-09-15T14:30:00")),
            SavingsGoal(id = "f8a9b0c1-d2e3-4567-8901-234567123456", name = "Bảo hiểm nhân thọ", targetAmount = 15000000.0, currentAmount = 5000000.0, category = "Sức khỏe", priority = "Cao", deadline = LocalDateTime.parse("2024-10-01T00:00:00"), createdAt = LocalDateTime.parse("2023-10-01T09:00:00"), iconResId = R.drawable.ic_person_heart),
            SavingsGoal(id = "a9b0c1d2-e3f4-5678-9012-345678234567", name = "Nghỉ hưu an nhàn", targetAmount = 200000000.0, currentAmount = 10000000.0, category = "Tiết kiệm chung", priority = "Thấp", deadline = LocalDateTime.parse("2040-01-01T00:00:00"), createdAt = LocalDateTime.parse("2020-01-01T00:00:00"), iconResId = R.drawable.ic_beach)
        )
    }

    fun getDepositLogs(existingGoals: List<SavingsGoal>): List<DepositLog> {
        val deposits = mutableListOf<DepositLog>()
        if (existingGoals.isEmpty()) return deposits

        for (i in 1..50) {
            val randomGoal = existingGoals.random()
            val transactionDate = randomGoal.createdAt.plusDays(Random.nextLong(1, 30))

            deposits.add(
                DepositLog(
                    id = UUID.randomUUID().toString(),
                    goalId = randomGoal.id,
                    goalName = randomGoal.name,
                    amount = Random.nextDouble(100000.0, 5000000.0),
                    transactionDate = transactionDate
                )
            )
        }
        return deposits
    }

    fun getNotificationLogs(): List<NotificationLog> {
        val notifications = mutableListOf<NotificationLog>()
        // Tiêu đề tiếng Việt
        val titles = listOf(
            "Cố lên bạn ơi!",
            "Nhắc nhở mục tiêu",
            "Nạp tiền thành công",
            "Đã đi được nửa chặng đường",
            "Sắp đến hạn rồi"
        )

        val messages = listOf(
            "Đừng quên tiết kiệm cho mục tiêu của bạn hôm nay nhé.",
            "Bạn vừa tiến thêm một bước gần hơn tới ước mơ.",
            "Hãy kiểm tra tiến độ tiết kiệm của bạn ngay.",
            "Một khoản tiết kiệm nhỏ hôm nay, một tương lai lớn ngày mai."
        )

        for (i in 1..20) {
            val isRead = Random.nextBoolean()
            val timestamp = LocalDateTime.now().minusHours(Random.nextLong(1, 720)) // Trong 30 ngày qua

            notifications.add(
                NotificationLog(
                    id = UUID.randomUUID().toString(),
                    title = titles.random(),
                    message = messages.random(),
                    timestamp = timestamp,
                    isRead = isRead
                )
            )
        }
        return notifications
    }
}