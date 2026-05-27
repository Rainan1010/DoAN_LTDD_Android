package com.example.doan_ltdd

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Màn hình Cài đặt (Settings).
 * Chức năng chính:
 * 1. Cài đặt giờ thông báo nhắc nhở (Lưu vào SharedPreferences).
 * 2. [ADMIN ONLY] Chức năng tạo dữ liệu mẫu (Reset dữ liệu giả lập).
 */
class SettingsActivity : AppCompatActivity() {

    // --- Khai báo các thành phần UI ---
    private lateinit var btnBack: ImageView
    private lateinit var cardGlobalTime: MaterialCardView
    private lateinit var tvGlobalTime: TextView

    // [MỚI] Các thành phần dành riêng cho Admin
    private lateinit var cardGenerateData: MaterialCardView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Khởi tạo Database (Singleton)
        database = AppDatabase.getDatabase(this)

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btnBackSettings)
        cardGlobalTime = findViewById(R.id.cardGlobalTime)
        tvGlobalTime = findViewById(R.id.tvGlobalTime)
        cardGenerateData = findViewById(R.id.cardGenerateData)

        // --- Kiểm tra quyền Admin ---
        // Lấy role được truyền từ màn hình Đăng nhập hoặc Home
        // Logic: Chỉ hiển thị nút "Tạo dữ liệu mẫu" nếu là ADMIN
        val role = intent.getStringExtra("USER_ROLE_KEY") ?: "USER"
        if (role == "ADMIN") {
            cardGenerateData.visibility = View.VISIBLE
        } else {
            cardGenerateData.visibility = View.GONE // Ẩn hoàn toàn với User thường
        }

        // 2. Xử lý sự kiện click
        btnBack.setOnClickListener { finish() }

        // Load giờ đã lưu để hiển thị lên màn hình ngay khi mở
        loadSavedTime()

        // Sự kiện click vào thẻ chọn giờ -> Hiện TimePicker
        cardGlobalTime.setOnClickListener {
            showTimePicker()
        }

        // [MỚI] Sự kiện click nút tạo dữ liệu (Chỉ Admin mới bấm được)
        cardGenerateData.setOnClickListener {
            showGenerateDataConfirmation()
        }
    }

    /**
     * Hàm đọc giờ đã lưu trong SharedPreferences (Bộ nhớ cài đặt nhẹ).
     * Mặc định là 09:00 nếu chưa cài đặt bao giờ.
     */
    private fun loadSavedTime() {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val hour = sharedPref.getInt("global_hour", 9)
        val minute = sharedPref.getInt("global_minute", 0)

        // Format hiển thị dạng 09:05 (thêm số 0 đằng trước nếu < 10)
        tvGlobalTime.text = String.format("%02d:%02d", hour, minute)
    }

    /**
     * Hiển thị hộp thoại chọn giờ hệ thống (TimePickerDialog).
     */
    private fun showTimePicker() {
        // Lấy giờ hiện tại để set default cho Dialog
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val currentHour = sharedPref.getInt("global_hour", 9)
        val currentMinute = sharedPref.getInt("global_minute", 0)

        // Tạo và hiển thị Dialog
        TimePickerDialog(this, { _, hourOfDay, minute ->
            // Khi người dùng bấm OK:
            // 1. Lưu vào SharedPreferences (commit async với apply())
            with(sharedPref.edit()) {
                putInt("global_hour", hourOfDay)
                putInt("global_minute", minute)
                apply()
            }
            // 2. Cập nhật giao diện text
            tvGlobalTime.text = String.format("%02d:%02d", hourOfDay, minute)

            // 3. Thông báo thành công
            Toast.makeText(this, "Đã lưu giờ thông báo mới!", Toast.LENGTH_SHORT).show()
        }, currentHour, currentMinute, true).show() // true = chế độ 24h
    }

    /**
     * Hiển thị cảnh báo trước khi tạo dữ liệu mẫu (vì hành động này ghi đè data).
     */
    private fun showGenerateDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Tạo dữ liệu mẫu")
            .setMessage("Hệ thống sẽ thêm các danh mục và mục tiêu mẫu vào ứng dụng. Bạn có chắc chắn muốn tiếp tục?")
            .setPositiveButton("Tạo ngay") { _, _ ->
                generateSampleData() // Gọi hàm xử lý logic
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Logic tạo dữ liệu mẫu.
     * QUAN TRỌNG: Chạy trên Coroutine (Dispatchers.IO) để tránh treo màn hình.
     */
    private fun generateSampleData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Lấy các DAO cần thiết
            val savingsDao = database.savingsDao()
            val categoryDao = database.categoryDao()
            val notificationDao = database.notificationDao()

            // 1. Khởi tạo danh mục (Ăn uống, Mua sắm...)
            // Sử dụng SampleDataProvider (Class tiện ích bên ngoài)
            val defaultCategories = SampleDataProvider.getAllCategories()
            defaultCategories.forEach { name ->
                // Insert category (CategoryDAO nên có onConflict = IGNORE để không lỗi nếu trùng tên)
                categoryDao.insert(Category(name))
            }

            // 2. Khởi tạo Goals, Logs, Notifications mẫu
            val goals = SampleDataProvider.getSavingsGoals()
            val deposits = SampleDataProvider.getDepositLogs(goals)
            val notifications = SampleDataProvider.getNotificationLogs()

            // Insert hàng loạt vào DB
            savingsDao.insertAllGoals(goals)
            savingsDao.insertAllDeposits(deposits)
            notificationDao.insertAll(notifications)

            Log.d("DB_INIT", "Đã thêm dữ liệu mẫu thành công!")

            // 3. Quay về luồng chính (Main Thread) để hiển thị thông báo UI
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, "Đã tạo dữ liệu mẫu thành công!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}