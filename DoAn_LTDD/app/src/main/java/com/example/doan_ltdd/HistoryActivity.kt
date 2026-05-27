package com.example.doan_ltdd

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Activity quản lý màn hình Lịch sử Giao dịch.
 * Chức năng:
 * 1. Tab 1: Xem toàn bộ lịch sử nạp tiền (DepositLog) sắp xếp theo thời gian.
 * 2. Tab 2: Xem danh sách mục tiêu -> Bấm vào để xem lịch sử riêng của mục tiêu đó (Filter).
 */
class HistoryActivity : AppCompatActivity() {

    // --- Khai báo các thành phần UI ---
    private lateinit var tabLayout: TabLayout
    private lateinit var rvAllHistory: RecyclerView  // RecyclerView cho Tab 1 (Tất cả)
    private lateinit var rvGoalsList: RecyclerView   // RecyclerView cho Tab 2 (Danh sách mục tiêu)
    private lateinit var btnBack: ImageView

    // --- Khai báo Database & Adapter ---
    private lateinit var database: AppDatabase
    private lateinit var allHistoryAdapter: HistoryAdapter     // Adapter hiển thị Logs
    private lateinit var simpleGoalAdapter: SimpleGoalAdapter  // Adapter hiển thị danh sách Goals (dạng rút gọn)

    // Biến cache: Lưu trữ toàn bộ logs để lọc cục bộ (Client-side filtering)
    // Giúp tránh việc phải query lại database khi mở dialog chi tiết
    private var allLogsList: List<DepositLog> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Khởi tạo instance Database (Singleton)
        database = AppDatabase.getDatabase(this)

        setControl()
        setupRecyclerViews()
        setupTabs()
        loadData()
        setEvent()
    }

    // Ánh xạ View từ XML sang code
    private fun setControl() {
        tabLayout = findViewById(R.id.tabLayout)
        rvAllHistory = findViewById(R.id.rvAllHistory)
        rvGoalsList = findViewById(R.id.rvGoalsList)
        btnBack = findViewById(R.id.btnBack)
    }

    // Xử lý các sự kiện click đơn giản
    private fun setEvent() {
        btnBack.setOnClickListener { finish() } // Đóng Activity quay về màn hình trước
    }

    // Cấu hình RecyclerView và Adapter
    private fun setupRecyclerViews() {
        // 1. Cấu hình cho Tab "Tất cả lịch sử"
        allHistoryAdapter = HistoryAdapter()
        rvAllHistory.adapter = allHistoryAdapter
        rvAllHistory.layoutManager = LinearLayoutManager(this)

        // 2. Cấu hình cho Tab "Theo mục tiêu"
        // Callback: Khi người dùng bấm vào một mục tiêu trong danh sách
        // -> Gọi hàm showGoalSpecificHistory để hiện dialog chi tiết
        simpleGoalAdapter = SimpleGoalAdapter { selectedGoal ->
            showGoalSpecificHistory(selectedGoal)
        }
        rvGoalsList.adapter = simpleGoalAdapter
        rvGoalsList.layoutManager = LinearLayoutManager(this)
    }

    // Tải dữ liệu từ Database
    private fun loadData() {
        // 1. Lấy tất cả Logs (Lịch sử giao dịch)
        // Sử dụng lifecycleScope để lắng nghe Flow -> Tự động cập nhật UI khi DB thay đổi
        lifecycleScope.launch {
            database.savingsDao().getAllLogs().collect { logs ->
                allLogsList = logs // Lưu vào biến tạm để dùng cho chức năng lọc sau này
                allHistoryAdapter.submitList(logs) // Đẩy dữ liệu vào Adapter hiển thị ngay
            }
        }

        // 2. Lấy danh sách Goals (Mục tiêu)
        lifecycleScope.launch {
            database.savingsDao().getAllGoals().collect { goals ->
                simpleGoalAdapter.submitList(goals)
            }
        }
    }

    // Cấu hình chuyển đổi giữa 2 Tab
    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Logic: Ẩn hiện RecyclerView tương ứng với Tab được chọn
                when (tab?.position) {
                    0 -> { // Tab "Tất cả"
                        rvAllHistory.visibility = View.VISIBLE
                        rvGoalsList.visibility = View.GONE
                    }
                    1 -> { // Tab "Theo mục tiêu"
                        rvAllHistory.visibility = View.GONE
                        rvGoalsList.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {} // Không dùng
            override fun onTabReselected(tab: TabLayout.Tab?) {} // Không dùng
        })
    }

    /**
     * Hàm hiển thị Dialog chứa lịch sử giao dịch của RIÊNG 1 mục tiêu.
     * @param goal: Mục tiêu người dùng vừa chọn.
     */
    private fun showGoalSpecificHistory(goal: SavingsGoal) {
        // Bước 1: Lọc dữ liệu từ list đã cache (allLogsList)
        // Chỉ lấy những log có goalId trùng với id của mục tiêu được chọn
        val specificLogs = allLogsList.filter { it.goalId == goal.id }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lịch sử: ${goal.name}")

        // Bước 2: Tạo RecyclerView bằng code (Programmatic UI)
        // Không cần tạo file XML layout riêng vì chỉ cần 1 list đơn giản
        val rvSpecific = RecyclerView(this)
        rvSpecific.layoutManager = LinearLayoutManager(this)

        // Bước 3: Gắn Adapter và dữ liệu đã lọc vào RecyclerView này
        val dialogAdapter = HistoryAdapter()
        dialogAdapter.submitList(specificLogs)
        rvSpecific.adapter = dialogAdapter
        rvSpecific.setPadding(32, 32, 32, 32) // Thêm padding cho thoáng

        // Đưa RecyclerView vào trong Dialog
        builder.setView(rvSpecific)

        // Kiểm tra nếu không có dữ liệu thì hiện thông báo
        if (specificLogs.isEmpty()) {
            builder.setMessage("Chưa có giao dịch nạp tiền nào cho mục tiêu này.")
        }

        builder.setPositiveButton("Đóng", null)
        builder.show()
    }
}