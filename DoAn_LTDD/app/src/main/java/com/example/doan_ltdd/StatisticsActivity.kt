package com.example.doan_ltdd

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var progressTotal: CircularProgressIndicator
    private lateinit var tvTotalPercent: TextView
    private lateinit var tvTotalCurrent: TextView
    private lateinit var tvTotalTarget: TextView
    private lateinit var rvCategoryStats: RecyclerView

    // Khai báo Database và Adapter
    private lateinit var database: AppDatabase
    private lateinit var adapter: CategoryStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Khởi tạo instance của Database
        database = AppDatabase.getDatabase(this)

        // Gọi các hàm thiết lập ban đầu
        setControl() // Ánh xạ View
        setEvent()   // Cài đặt sự kiện
        loadData()   // Tải và xử lý dữ liệu
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBackStats)
        progressTotal = findViewById(R.id.progressTotal)
        tvTotalPercent = findViewById(R.id.tvTotalPercent)
        tvTotalCurrent = findViewById(R.id.tvTotalCurrent)
        tvTotalTarget = findViewById(R.id.tvTotalTarget)
        rvCategoryStats = findViewById(R.id.rvCategoryStats)

        // Setup RecyclerView: Khởi tạo Adapter và LayoutManager
        adapter = CategoryStatsAdapter()
        rvCategoryStats.layoutManager = LinearLayoutManager(this)
        rvCategoryStats.adapter = adapter
    }

    /**
     * Hàm setEvent: Xử lý các sự kiện tương tác của người dùng.
     */
    private fun setEvent() {
        // Xử lý khi nhấn nút Back -> Đóng Activity hiện tại
        btnBack.setOnClickListener { finish() }
    }

    /**
     * Hàm loadData: Lấy dữ liệu từ Room Database, tính toán thống kê và cập nhật UI.
     * Sử dụng Coroutine (lifecycleScope) để xử lý bất đồng bộ.
     */
    private fun loadData() {
        // Định dạng tiền tệ theo chuẩn Việt Nam
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        lifecycleScope.launch {
            // Lấy toàn bộ danh sách mục tiêu (Goal) từ DB dưới dạng Flow để cập nhật realtime
            database.savingsDao().getAllGoals().collect { goals ->

                // --- 1. TÍNH TOÁN THỐNG KÊ TỔNG QUAN ---

                // Tính tổng số tiền hiện tại của tất cả các goal
                val totalCurrent = goals.sumOf { it.currentAmount }
                // Tính tổng số tiền mục tiêu của tất cả các goal
                val totalTarget = goals.sumOf { it.targetAmount }

                // Tính phần trăm hoàn thành tổng thể
                // Kiểm tra chia cho 0 nếu totalTarget chưa có dữ liệu
                val totalProgress = if (totalTarget > 0) {
                    ((totalCurrent / totalTarget) * 100).toInt()
                } else 0

                // Cập nhật giao diện phần tổng quan
                progressTotal.setProgress(totalProgress, true) // setProgress với animation
                tvTotalPercent.text = "$totalProgress%"
                tvTotalCurrent.text = formatter.format(totalCurrent)
                tvTotalTarget.text = formatter.format(totalTarget)

                // --- 2. TÍNH TOÁN THỐNG KÊ CHI TIẾT THEO DANH MỤC ---

                // Gom nhóm các goal có cùng tên danh mục (category) vào một Map
                // Key: Tên danh mục, Value: List các goal thuộc danh mục đó
                val groupedMap = goals.groupBy { it.category }

                val categoryStatsList = mutableListOf<CategoryStat>()

                // Duyệt qua từng nhóm danh mục để tính toán số liệu tổng hợp cho nhóm đó
                for ((categoryName, goalsInGroup) in groupedMap) {
                    // Tổng tiền hiện tại của nhóm
                    val groupCurrent = goalsInGroup.sumOf { it.currentAmount }
                    // Tổng tiền mục tiêu của nhóm
                    val groupTarget = goalsInGroup.sumOf { it.targetAmount }

                    // Thêm vào danh sách hiển thị
                    categoryStatsList.add(CategoryStat(categoryName, groupCurrent, groupTarget))
                }

                // Sắp xếp danh sách theo số tiền đã tiết kiệm giảm dần
                categoryStatsList.sortByDescending { it.totalCurrent }

                // Cập nhật dữ liệu vào Adapter để hiển thị lên RecyclerView
                adapter.submitList(categoryStatsList)
            }
        }
    }
}