package com.example.doan_ltdd

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar

/**
 * MainActivity: Màn hình chính của ứng dụng.
 * Chức năng:
 * - Hiển thị danh sách mục tiêu (RecyclerView).
 * - Quản lý Menu trượt (Navigation Drawer).
 * - Xử lý thêm/sửa/xóa mục tiêu.
 * - Xử lý tìm kiếm, nạp tiền và thông báo.
 */
class MainActivity : AppCompatActivity() {

    // Khai báo Database và Adapter
    private lateinit var database: AppDatabase
    private lateinit var goalAdapter: GoalAdapter

    // Khai báo các View (Giao diện)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvGoals: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnMenu: ImageView
    private lateinit var navigationView: NavigationView
    private lateinit var btnNotification: ImageView
    private lateinit var btnSearch: ImageView

    // Biến lưu danh sách gốc để phục vụ chức năng tìm kiếm/lọc
    private var originalGoals: List<SavingsGoal> = listOf()

    // Biến lưu User hiện tại và Username nhận từ LoginActivity
    private var currentUser: User? = null
    private var currentUsername: String = ""
    private var currentUserRole: String = "USER"

    // Danh sách tài nguyên hình ảnh (Icon) cho mục tiêu
    private val availableIcons = listOf(
        R.drawable.ic_beach,
        R.drawable.ic_car,
        R.drawable.ic_home,
        R.drawable.ic_health,
        R.drawable.ic_heart_smile,
        R.drawable.ic_person_heart,
        R.drawable.ic_mobile,
        R.drawable.ic_motorcycle,
        R.drawable.ic_pets,
        R.drawable.ic_partner,
        R.drawable.ic_travel
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Kích hoạt chế độ tràn viền
        setContentView(R.layout.activity_main)

        // Xử lý Window Insets để nội dung không bị che bởi thanh trạng thái/điều hướng
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo Database
        database = AppDatabase.getDatabase(this)

        // 1. Nhận dữ liệu (Username và Role) được truyền từ LoginActivity
        currentUsername = intent.getStringExtra("USERNAME_KEY") ?: ""
        currentUserRole = intent.getStringExtra("USER_ROLE_KEY") ?: "USER"

        // 2. Thiết lập giao diện và sự kiện
        setControl()
        setEvent()

        // 3. Kiểm tra và tạo thông báo nhắc nhở
        checkAndSendNotifications()

        // 4. Load thông tin User lên Header của Menu trượt
        loadUserInfo()
    }

    // Hàm ánh xạ View và thiết lập trạng thái ban đầu
    private fun setControl() {
        drawerLayout = findViewById(R.id.drawerLayout)
        rvGoals = findViewById(R.id.rvGoals)
        fabAdd = findViewById(R.id.fabAdd)
        btnMenu = findViewById(R.id.btnMenu)
        navigationView = findViewById(R.id.navigationView)
        btnNotification = findViewById(R.id.btnNotification)
        btnSearch = findViewById(R.id.btnSearch)

        // [PHÂN QUYỀN] Kiểm tra xem user có phải là ADMIN không
        val isAdmin = (currentUserRole == "ADMIN")

        // 1. Khởi tạo Adapter cho RecyclerView
        // Truyền biến isAdmin vào
        goalAdapter = GoalAdapter(
            isAdmin = isAdmin,
            onDepositClick = { goal -> showDepositDialog(goal) }, // Sự kiện click vào nạp tiền
            onMenuClick = { goal, view -> showGoalOptionMenu(goal, view) } // Sự kiện click Menu tùy chọn
        )

        rvGoals.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = goalAdapter
        }

        // 2. Ẩn nút "Thêm mục tiêu" nếu người dùng không phải là Admin
        if (isAdmin) {
            fabAdd.visibility = View.VISIBLE
        } else {
            fabAdd.visibility = View.GONE
        }
    }

    // Hàm thiết lập các sự kiện tương tác
    private fun setEvent() {
        // Lấy danh sách Goals từ Database (Sử dụng Flow để tự động cập nhật UI khi dữ liệu thay đổi)
        lifecycleScope.launch {
            database.savingsDao().getAllGoals().collect { goals ->
                originalGoals = goals // Lưu bản sao để dùng cho tìm kiếm
                goalAdapter.submitList(goals) // Đẩy dữ liệu vào RecyclerView
            }
        }

        // Sự kiện click nút Menu -> Mở Drawer (Menu trượt bên trái)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Sự kiện click nút Thêm (+) -> Mở Dialog thêm mục tiêu
        fabAdd.setOnClickListener {
            showAddGoalDialog()
        }

        // Sự kiện click nút Tìm kiếm -> Mở Dialog tìm kiếm/lọc
        btnSearch.setOnClickListener {
            showSearchDialog()
        }

        // Xử lý sự kiện khi chọn item trong Navigation Menu (Menu trái)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> drawerLayout.closeDrawer(GravityCompat.START) // Đang ở Home thì chỉ đóng menu
                R.id.nav_history -> { // Chuyển sang màn hình Lịch sử
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_statistics -> { // Chuyển sang màn hình Thống kê
                    val intent = Intent(this, StatisticsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_settings -> { // Chuyển sang màn hình Cài đặt
                    val intent = Intent(this, SettingsActivity::class.java)
                    // Truyền Role sang Settings để ẩn/hiện nút "Tạo dữ liệu mẫu"
                    intent.putExtra("USER_ROLE_KEY", currentUserRole)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_about -> { // Chuyển sang màn hình Giới thiệu
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }

        // Sự kiện click nút Chuông -> Mở màn hình danh sách Thông báo
        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    // --- CÁC HÀM XỬ LÝ USER & HEADER (THÔNG TIN NGƯỜI DÙNG) ---

    // Lấy thông tin User từ Database dựa vào username
    private fun loadUserInfo() {
        if (currentUsername.isNotEmpty()) {
            lifecycleScope.launch {
                currentUser = database.userDao().getUserByUsername(currentUsername)
                currentUser?.let { user ->
                    updateNavHeader(user) // Cập nhật giao diện Header
                }
            }
        }
    }

    // Cập nhật giao diện phần Header của Menu trượt (Avatar, Tên, Email)
    private fun updateNavHeader(user: User) {
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        val imgAvatar = headerView.findViewById<ImageView>(R.id.imgAvatar)
        val btnLogout = headerView.findViewById<ImageView>(R.id.btnLogout)

        // Hiển thị tên và email (nếu có)
        tvUserName.text = if (user.fullName.isNotBlank()) user.fullName else user.username
        tvUserEmail.text = if (user.email.isNotBlank()) user.email else "Cập nhật email ngay"

        // Xử lý nút Đăng xuất
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Xóa back stack để không thể quay lại màn hình chính bằng nút Back
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Click vào Avatar -> Mở Dialog cập nhật thông tin cá nhân
        imgAvatar.setOnClickListener {
            showUpdateProfileDialog()
        }
    }

    // Hiển thị Dialog cập nhật thông tin cá nhân (Tên, Email, Mật khẩu)
    private fun showUpdateProfileDialog() {
        val user = currentUser ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_profile, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Ánh xạ view trong dialog
        val etName = dialogView.findViewById<EditText>(R.id.etUpdateFullName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etUpdateEmail)
        val etPass = dialogView.findViewById<EditText>(R.id.etUpdatePass)
        val btnSave = dialogView.findViewById<Button>(R.id.btnUpdateSave)

        // Điền dữ liệu cũ
        etName.setText(user.fullName)
        etEmail.setText(user.email)

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newPass = etPass.text.toString().trim()

            // Tạo object User mới với thông tin cập nhật
            val updatedUser = user.copy(
                fullName = newName,
                email = newEmail,
                password = if (newPass.isNotEmpty()) newPass else user.password
            )

            // Lưu vào Database
            lifecycleScope.launch {
                database.userDao().updateUser(updatedUser)
                currentUser = updatedUser
                updateNavHeader(updatedUser)
                Toast.makeText(this@MainActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    // --- CÁC HÀM XỬ LÝ DIALOG (HỘP THOẠI) ---

    // Hiển thị Dialog Thêm mới hoặc Chỉnh sửa Mục tiêu
    // Nếu goalToEdit != null -> Chế độ Chỉnh sửa
    private fun showAddGoalDialog(goalToEdit: SavingsGoal? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Ánh xạ view dialog
        val ivGoalIcon = dialogView.findViewById<ImageView>(R.id.ivGoalIcon)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etGoalName = dialogView.findViewById<EditText>(R.id.etGoalName)
        val etTargetAmount = dialogView.findViewById<EditText>(R.id.etTargetAmount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnAddCategory = dialogView.findViewById<ImageView>(R.id.btnAddCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val etDeadline = dialogView.findViewById<EditText>(R.id.etDeadline)

        // Setup Spinner độ ưu tiên
        val priorities = listOf("Thấp", "Trung bình", "Cao")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)

        // Setup Spinner danh mục (Lấy từ Database)
        val categoryList = mutableListOf<String>()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        spinnerCategory.adapter = categoryAdapter

        lifecycleScope.launch {
            database.categoryDao().getAllCategories().collect { categories ->
                categoryList.clear()
                categoryList.addAll(categories.map { it.name })
                categoryAdapter.notifyDataSetChanged()

                // Nếu đang chỉnh sửa, set danh mục cũ làm mặc định
                if (goalToEdit != null) {
                    val index = categoryList.indexOf(goalToEdit.category)
                    if (index >= 0) spinnerCategory.setSelection(index)
                }
            }
        }

        // Sự kiện thêm nhanh danh mục mới
        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        var selectedDate: LocalDateTime = LocalDateTime.now().plusMonths(1)
        var selectedIconId: Int = availableIcons[0]

        // Nếu là chế độ Chỉnh sửa, điền dữ liệu cũ vào form
        if (goalToEdit != null) {
            tvDialogTitle.text = "Chỉnh Sửa Mục Tiêu"
            etGoalName.setText(goalToEdit.name)
            etTargetAmount.setText(String.format("%.0f", goalToEdit.targetAmount))
            selectedIconId = goalToEdit.iconResId
            ivGoalIcon.setImageResource(selectedIconId)

            val priorityIndex = priorities.indexOf(goalToEdit.priority)
            if (priorityIndex >= 0) spinnerPriority.setSelection(priorityIndex)

            selectedDate = goalToEdit.deadline
            etDeadline.setText("${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}")
        } else {
            etDeadline.setText("${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}")
        }

        // Xử lý chọn ngày (DatePicker)
        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (goalToEdit != null) calendar.set(goalToEdit.deadline.year, goalToEdit.deadline.monthValue - 1, goalToEdit.deadline.dayOfMonth)
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = LocalDateTime.of(year, month + 1, day, 23, 59)
                etDeadline.setText("$day/${month + 1}/$year")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Xử lý chọn Icon
        ivGoalIcon.setOnClickListener {
            val iconAdapter = object : ArrayAdapter<Int>(this, android.R.layout.select_dialog_item, availableIcons) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.text = ""
                    view.setCompoundDrawablesWithIntrinsicBounds(availableIcons[position], 0, 0, 0)
                    view.compoundDrawablePadding = 24
                    return view
                }
            }
            AlertDialog.Builder(this)
                .setTitle("Chọn biểu tượng")
                .setAdapter(iconAdapter) { dialogInterface, which ->
                    selectedIconId = availableIcons[which]
                    ivGoalIcon.setImageResource(selectedIconId)
                    dialogInterface.dismiss()
                }
                .create()
                .show()
        }

        // Xử lý nút Lưu
        btnSave.setOnClickListener {
            val name = etGoalName.text.toString()
            val amountStr = etTargetAmount.text.toString()
            val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "Khác"

            if (name.isBlank() || amountStr.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập tên và số tiền!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (goalToEdit == null) {
                    // Tạo mới
                    val newGoal = SavingsGoal(
                        name = name,
                        targetAmount = amountStr.toDouble(),
                        category = selectedCategory,
                        priority = spinnerPriority.selectedItem.toString(),
                        deadline = selectedDate,
                        iconResId = selectedIconId
                    )
                    database.savingsDao().insertGoal(newGoal)
                    Toast.makeText(this@MainActivity, "Đã thêm mục tiêu!", Toast.LENGTH_SHORT).show()
                } else {
                    // Cập nhật
                    val updatedGoal = goalToEdit.copy(
                        name = name,
                        targetAmount = amountStr.toDouble(),
                        category = selectedCategory,
                        priority = spinnerPriority.selectedItem.toString(),
                        deadline = selectedDate,
                        iconResId = selectedIconId
                    )
                    database.savingsDao().updateGoal(updatedGoal)
                    Toast.makeText(this@MainActivity, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Dialog thêm nhanh Danh mục
    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Nhập tên danh mục mới"
        input.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle("Thêm Danh Mục")
            .setView(input)
            .setPositiveButton("Thêm") { _, _ ->
                val newCategoryName = input.text.toString().trim()
                if (newCategoryName.isNotEmpty()) {
                    lifecycleScope.launch {
                        database.categoryDao().insert(Category(newCategoryName))
                        Toast.makeText(this@MainActivity, "Đã thêm danh mục: $newCategoryName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Dialog Tìm kiếm và Lọc
    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etSearchName = dialogView.findViewById<EditText>(R.id.etSearchName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerSearchCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerSearchPriority)
        val btnSearchAction = dialogView.findViewById<Button>(R.id.btnPerformSearch)
        val btnReset = dialogView.findViewById<Button>(R.id.btnResetSearch)

        // Setup Spinner Danh mục cho tìm kiếm
        val categoryList = mutableListOf<String>()
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        spinnerCategory.adapter = catAdapter

        lifecycleScope.launch {
            database.categoryDao().getAllCategories().collect { categories ->
                categoryList.clear()
                categoryList.add("Tất cả")
                categoryList.addAll(categories.map { it.name })
                catAdapter.notifyDataSetChanged()
            }
        }

        // Setup Spinner Ưu tiên cho tìm kiếm
        val priorities = listOf("Tất cả", "Thấp", "Trung bình", "Cao")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)

        // Thực hiện tìm kiếm (Lọc trên RAM từ list originalGoals)
        btnSearchAction.setOnClickListener {
            val keyword = etSearchName.text.toString().trim().lowercase()
            val selectedCategory = spinnerCategory.selectedItem.toString()
            val selectedPriority = spinnerPriority.selectedItem.toString()

            val filteredList = originalGoals.filter { goal ->
                val matchName = keyword.isEmpty() || goal.name.lowercase().contains(keyword)
                val matchCategory = selectedCategory == "Tất cả" || goal.category == selectedCategory
                val matchPriority = selectedPriority == "Tất cả" || goal.priority == selectedPriority
                matchName && matchCategory && matchPriority
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy kết quả nào!", Toast.LENGTH_SHORT).show()
            }
            goalAdapter.submitList(filteredList)
            dialog.dismiss()
        }

        // Đặt lại tìm kiếm (Hiển thị toàn bộ)
        btnReset.setOnClickListener {
            goalAdapter.submitList(originalGoals)
            dialog.dismiss()
            Toast.makeText(this, "Đã hiển thị lại tất cả mục tiêu", Toast.LENGTH_SHORT).show()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Dialog Nạp tiền vào mục tiêu
    private fun showDepositDialog(goal: SavingsGoal) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_deposit, null)

        val tvCurrentGoalName = dialogView.findViewById<TextView>(R.id.tvCurrentGoalName)
        val etDepositAmount = dialogView.findViewById<EditText>(R.id.etDepositAmount)
        val btnConfirmDeposit = dialogView.findViewById<Button>(R.id.btnConfirmDeposit)
        val btnCancelDeposit = dialogView.findViewById<Button>(R.id.btnCancelDeposit)

        tvCurrentGoalName.text = "Cho mục tiêu: ${goal.name}"

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnConfirmDeposit.setOnClickListener {
            val amountStr = etDepositAmount.text.toString()
            if (amountStr.isNotBlank()) {
                val amount = amountStr.toDouble()

                // Cập nhật số tiền hiện tại của Goal
                val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
                // Tạo log lịch sử giao dịch
                val log = DepositLog(
                    goalId = updatedGoal.id,
                    goalName = updatedGoal.name,
                    amount = amount
                )

                lifecycleScope.launch {
                    database.savingsDao().updateGoal(updatedGoal)
                    database.savingsDao().insertLog(log)

                    dialog.dismiss()
                    Toast.makeText(this@MainActivity, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancelDeposit.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Hiển thị Menu tùy chọn (3 chấm) trên mỗi Goal (Chỉ Admin mới thấy)
    private fun showGoalOptionMenu(goal: SavingsGoal, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_goal_options, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notification -> {
                    showNotificationDialog(goal)
                    true
                }
                R.id.action_edit -> {
                    showAddGoalDialog(goal)
                    true
                }
                R.id.action_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa mục tiêu '${goal.name}'?")
                        .setPositiveButton("Xóa") { _, _ ->
                            lifecycleScope.launch {
                                database.savingsDao().deleteGoal(goal)
                            }
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // Dialog cài đặt thông báo cho từng Goal
    private fun showNotificationDialog(goal: SavingsGoal) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val switchNotification = dialogView.findViewById<SwitchCompat>(R.id.switchNotification)
        val layoutOptions = dialogView.findViewById<LinearLayout>(R.id.layoutOptions)
        val rbMonthly = dialogView.findViewById<RadioButton>(R.id.rbMonthly)
        val rbDaily = dialogView.findViewById<RadioButton>(R.id.rbDaily)
        val spinnerDayOfMonth = dialogView.findViewById<Spinner>(R.id.spinnerDayOfMonth)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveNoti)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelNoti)

        val days = (1..31).map { "Ngày $it" }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)
        spinnerDayOfMonth.adapter = spinnerAdapter

        // Khôi phục trạng thái cũ
        switchNotification.isChecked = goal.isReminderEnabled

        if (goal.reminderFrequency == "MONTHLY") {
            rbMonthly.isChecked = true
            rbDaily.isChecked = false
        } else {
            rbMonthly.isChecked = false
            rbDaily.isChecked = true
        }

        if (goal.reminderDayOfMonth in 1..31) {
            spinnerDayOfMonth.setSelection(goal.reminderDayOfMonth - 1)
        }

        // Logic ẩn hiện tùy chọn khi bật/tắt Switch
        fun updateUIState(isEnabled: Boolean) {
            layoutOptions.alpha = if (isEnabled) 1.0f else 0.5f
            rbMonthly.isEnabled = isEnabled
            rbDaily.isEnabled = isEnabled
            spinnerDayOfMonth.isEnabled = isEnabled && rbMonthly.isChecked
        }
        updateUIState(switchNotification.isChecked)

        switchNotification.setOnCheckedChangeListener { _, isChecked -> updateUIState(isChecked) }

        rbMonthly.setOnClickListener {
            rbDaily.isChecked = false
            updateUIState(true)
        }
        rbDaily.setOnClickListener {
            rbMonthly.isChecked = false
            updateUIState(true)
        }

        btnSave.setOnClickListener {
            val updatedGoal = goal.copy(
                isReminderEnabled = switchNotification.isChecked
            )

            if (updatedGoal.isReminderEnabled) {
                if (rbMonthly.isChecked) {
                    updatedGoal.reminderFrequency = "MONTHLY"
                    updatedGoal.reminderDayOfMonth = spinnerDayOfMonth.selectedItemPosition + 1
                } else {
                    updatedGoal.reminderFrequency = "DAILY"
                }
            }

            lifecycleScope.launch {
                database.savingsDao().updateGoal(updatedGoal)
                Toast.makeText(this@MainActivity, "Đã lưu cài đặt thông báo!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Hàm chạy ngầm để kiểm tra và gửi thông báo nhắc nhở
    private fun checkAndSendNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            val goals = database.savingsDao().getAllGoalsList()
            val now = LocalDateTime.now()
            val startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0)

            // Lấy giờ thông báo chung được lưu trong SharedPref (Cài đặt)
            val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val globalHour = sharedPref.getInt("global_hour", 9)
            val globalMinute = sharedPref.getInt("global_minute", 0)

            goals.forEach { goal ->
                // 1. Kiểm tra nhắc nhở định kỳ (Hằng ngày/Hằng tháng)
                if (goal.isReminderEnabled) {
                    var shouldNotify = false
                    val isPastTime = (now.hour > globalHour) || (now.hour == globalHour && now.minute >= globalMinute)

                    if (goal.reminderFrequency == "DAILY") {
                        if (isPastTime) {
                            shouldNotify = true
                        }
                    } else if (goal.reminderFrequency == "MONTHLY") {
                        if (now.dayOfMonth == goal.reminderDayOfMonth && isPastTime) {
                            shouldNotify = true
                        }
                    }

                    if (shouldNotify) {
                        createNotificationIfNotExists(
                            "Nhắc nhở: ${goal.name}",
                            "Đừng quên tiết kiệm cho mục tiêu ${goal.name} nhé!",
                            startOfDay
                        )
                    }
                }

                // 2. Kiểm tra sắp đến hạn Deadline (còn dưới 3 ngày)
                if (goal.currentAmount < goal.targetAmount && now.hour >= 9) {
                    val daysLeft = ChronoUnit.DAYS.between(now.toLocalDate(), goal.deadline.toLocalDate())
                    if (daysLeft in 0..3) {
                        val title = "Sắp đến hạn: ${goal.name}"
                        val msg = "Chỉ còn $daysLeft ngày nữa là đến hạn mục tiêu ${goal.name}."
                        createNotificationIfNotExists(title, msg, startOfDay)
                    }
                }
            }
        }
    }

    // Hàm tạo Notification Log trong DB nếu chưa tồn tại trong ngày hôm nay
    private suspend fun createNotificationIfNotExists(title: String, message: String, startOfDay: LocalDateTime) {
        val count = database.notificationDao().checkExistsToday(title, startOfDay)

        if (count == 0) {
            val log = NotificationLog(
                title = title,
                message = message,
                timestamp = LocalDateTime.now()
            )
            database.notificationDao().insert(log)
            // withContext(Dispatchers.Main) { showSystemNotification(title, message) } // hiện noti trên thanh trạng thái
        }
    }

    // Hàm hiển thị thông báo hệ thống (Notification Tray)
    private fun showSystemNotification(title: String, message: String) {
        val channelId = "savings_reminder_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Nhắc nhở tiết kiệm", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // notificationManager.notify(System.currentTimeMillis().toInt(), notification) // Yêu cầu quyền gưi thông báo
    }
}