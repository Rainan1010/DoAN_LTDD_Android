package com.example.doan_ltdd

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvNotifications: RecyclerView
    private lateinit var btnDeleteAll: ImageView // Khai báo nút xóa
    private lateinit var adapter: NotificationAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        database = AppDatabase.getDatabase(this)

        setControl()
        setEvent()
    }

    private fun setControl() {
        toolbar = findViewById(R.id.toolbarNoti)
        rvNotifications = findViewById(R.id.rvNotifications)
        btnDeleteAll = findViewById(R.id.btnDeleteAll) // Ánh xạ nút xóa

        adapter = NotificationAdapter()
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun setEvent() {
        // 1. Setup Toolbar (Nút Back)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // 2. Load dữ liệu
        lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collect { list ->
                adapter.submitList(list)
            }
        }

        // 3. Sự kiện Click nút Thùng rác (Xóa tất cả)
        btnDeleteAll.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    // Hộp thoại xác nhận trước khi xóa
    private fun showDeleteConfirmationDialog() {
        if (adapter.itemCount == 0) {
            Toast.makeText(this, "Danh sách trống!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Xóa tất cả?")
            .setMessage("Bạn có chắc muốn xóa sạch lịch sử thông báo không?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    database.notificationDao().clearAll()
                    Toast.makeText(this@NotificationActivity, "Đã xóa thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}