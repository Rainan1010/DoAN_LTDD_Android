package com.example.doan_ltdd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.NumberFormat
import java.util.Locale

/**
 * Data class phụ trợ (Model View) dùng riêng cho UI thống kê.
 * Class này không lưu trong Database, nó chỉ chứa dữ liệu đã được tính toán tổng hợp.
 */
data class CategoryStat(
    val name: String,          // Tên danh mục
    val totalCurrent: Double,  // Số tiền hiện tại đã có
    val totalTarget: Double    // Số tiền mục tiêu
) {
    // Thuộc tính tính toán tự động: Tính % hoàn thành
    // Logic: Nếu mục tiêu > 0 thì mới chia để tránh lỗi chia cho 0.
    val progress: Int
        get() = if (totalTarget > 0) ((totalCurrent / totalTarget) * 100).toInt() else 0
}

/**
 * Adapter cho RecyclerView để hiển thị danh sách thống kê theo danh mục.
 */
class CategoryStatsAdapter : RecyclerView.Adapter<CategoryStatsAdapter.ViewHolder>() {

    // Danh sách dữ liệu nguồn
    private var list = listOf<CategoryStat>()

    /**
     * Hàm cập nhật dữ liệu mới cho Adapter.
     * Khi gọi hàm này, danh sách cũ sẽ bị thay thế và giao diện được vẽ lại.
     */
    fun submitList(newList: List<CategoryStat>) {
        list = newList
        notifyDataSetChanged() // Thông báo cho RecyclerView biết dữ liệu đã thay đổi để vẽ lại
    }

    /**
     * ViewHolder: Class nắm giữ (cache) các thành phần giao diện (View) của 1 dòng item.
     * Giúp tránh việc phải gọi findViewById quá nhiều lần (tối ưu hiệu năng).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvPercent: TextView = view.findViewById(R.id.tvCategoryPercent)
        // Sử dụng LinearProgressIndicator của Material Design thay vì ProgressBar thường
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.progressCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvCategoryAmount)
    }

    /**
     * Bước 1: Tạo ra View.
     * Hàm này chạy khi RecyclerView cần tạo một dòng item mới.
     * Nó sẽ "thổi" (inflate) file layout XML (item_category_stat) thành đối tượng View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stat, parent, false)
        return ViewHolder(view)
    }

    /**
     * Bước 2: Đổ dữ liệu vào View (Binding).
     * Hàm này chạy liên tục mỗi khi một dòng item xuất hiện trên màn hình.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Lấy đối tượng dữ liệu tại vị trí tương ứng
        val item = list[position]

        // Gán tên và phần trăm
        holder.tvName.text = item.name
        holder.tvPercent.text = "${item.progress}%"

        // Cập nhật thanh tiến trình (Progress Bar)
        holder.progressBar.progress = item.progress

        // --- Xử lý định dạng tiền tệ (Currency Formatting) ---
        // Tạo formatter theo chuẩn Tiếng Việt (ví dụ: 100.000 đ)
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val current = formatter.format(item.totalCurrent)
        val target = formatter.format(item.totalTarget)

        // Hiển thị chuỗi dạng: "1.000.000 đ / 5.000.000 đ"
        holder.tvAmount.text = "$current / $target"
    }

    // Trả về tổng số lượng item có trong danh sách
    override fun getItemCount() = list.size
}