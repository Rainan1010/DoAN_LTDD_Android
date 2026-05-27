package com.example.doan_ltdd

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.NumberFormat
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Adapter hiển thị danh sách các Mục tiêu tiết kiệm (SavingsGoal).
 * Kế thừa từ ListAdapter (thay vì RecyclerView.Adapter thường) để tối ưu hiệu năng
 * và có sẵn animation khi thêm/xóa/sửa item nhờ DiffUtil.
 *
 * @param isAdmin: Biến cờ (Flag) để kiểm tra người dùng có phải Admin không.
 * @param onDepositClick: Hàm callback (sự kiện) khi người dùng bấm vào item để nạp tiền.
 * @param onMenuClick: Hàm callback khi người dùng bấm nút 3 chấm (chỉnh sửa/xóa).
 */
class GoalAdapter(
    private val isAdmin: Boolean,
    private val onDepositClick: (SavingsGoal) -> Unit,
    private val onMenuClick: (SavingsGoal, View) -> Unit
) : ListAdapter<SavingsGoal, GoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    // Tạo ViewHolder mới (Inflate layout item_goal.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    // Đổ dữ liệu vào ViewHolder tại vị trí position
    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Inner Class nắm giữ các View trong layout item_goal.xml
     */
    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // --- 1. Ánh xạ View (Tìm View theo ID) ---
        private val ivGoalIconItem: ImageView = itemView.findViewById(R.id.ivGoalIconItem)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progressBar)
        private val tvPercent: TextView = itemView.findViewById(R.id.tvPercent)
        private val tvPriority: Chip = itemView.findViewById(R.id.tvPriority) // Chip dùng để hiện mức độ ưu tiên
        private val layoutWarning: LinearLayout = itemView.findViewById(R.id.layoutWarning) // Khung cảnh báo hạn chót
        private val tvTimeRemaining: TextView = itemView.findViewById(R.id.tvTimeRemaining)
        private val btnMore: ImageView = itemView.findViewById(R.id.btnMore) // Nút 3 chấm

        /**
         * Hàm gán dữ liệu chi tiết cho từng item
         */
        fun bind(goal: SavingsGoal) {
            // --- Hiển thị thông tin cơ bản ---
            ivGoalIconItem.setImageResource(goal.iconResId) // Icon mục tiêu
            tvTitle.text = goal.name

            // --- Định dạng tiền tệ (Ví dụ: 100.000 đ / 5.000.000 đ) ---
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            val current = formatter.format(goal.currentAmount)
            val target = formatter.format(goal.targetAmount)
            tvAmount.text = "$current / $target"

            // --- Thanh tiến trình (Progress Bar) ---
            progressBar.progress = goal.progressPercentage
            tvPercent.text = "${goal.progressPercentage}%"

            // --- Xử lý màu sắc theo mức độ ưu tiên (Priority) ---
            // Cao: Đỏ nhạt / Trung bình: Xanh nhạt / Thấp: Xám
            tvPriority.text = goal.priority
            val (bgColor, textColor) = when (goal.priority) {
                "Cao" -> Pair(0xFFFFEBEE.toInt(), 0xFFD32F2F.toInt())       // Red
                "Trung bình" -> Pair(0xFFE3F2FD.toInt(), 0xFF1976D2.toInt()) // Blue
                else -> Pair(0xFFF5F5F5.toInt(), 0xFF616161.toInt())         // Grey
            }
            // Gán màu background và màu chữ cho Chip
            tvPriority.chipBackgroundColor = ColorStateList.valueOf(bgColor)
            tvPriority.setTextColor(textColor)

            // --- Logic cảnh báo hạn chót (Deadline Warning) ---
            // Nếu còn dưới 3 ngày VÀ chưa hoàn thành mục tiêu -> Hiện cảnh báo
            if (goal.isDeadlineApproaching(3) && goal.currentAmount < goal.targetAmount) {
                layoutWarning.visibility = View.VISIBLE

                // Tính khoảng cách ngày giữa hiện tại và deadline
                val daysLeft = ChronoUnit.DAYS.between(java.time.LocalDateTime.now(), goal.deadline)
                tvTimeRemaining.text = if (daysLeft < 0) "Đã quá hạn" else "$daysLeft ngày nữa"
            } else {
                layoutWarning.visibility = View.GONE // Ẩn đi nếu không cần cảnh báo
            }

            // --- Phân quyền Admin (Ẩn/Hiện nút Menu) ---
            if (isAdmin) {
                btnMore.visibility = View.VISIBLE
                // Chỉ admin mới click được nút sửa/xóa
                btnMore.setOnClickListener { onMenuClick(goal, btnMore) }
            } else {
                btnMore.visibility = View.GONE // User thường không thấy nút này
            }

            // Sự kiện click vào toàn bộ item (để vào màn hình chi tiết hoặc nạp tiền)
            itemView.setOnClickListener { onDepositClick(goal) }
        }
    }

    /**
     * Class so sánh sự khác biệt giữa danh sách cũ và mới.
     * Giúp RecyclerView chỉ vẽ lại những item thực sự thay đổi (Tối ưu hiệu năng).
     */
    class GoalDiffCallback : DiffUtil.ItemCallback<SavingsGoal>() {
        // So sánh ID (xem có phải cùng 1 item không)
        override fun areItemsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal) =
            oldItem.id == newItem.id

        // So sánh nội dung (xem dữ liệu bên trong có thay đổi không)
        override fun areContentsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal) =
            oldItem == newItem
    }
}