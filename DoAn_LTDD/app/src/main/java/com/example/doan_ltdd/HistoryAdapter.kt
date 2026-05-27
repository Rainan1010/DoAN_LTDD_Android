package com.example.doan_ltdd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryAdapter : ListAdapter<DepositLog, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoalName: TextView = view.findViewById(R.id.tvHistoryGoalName)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvAmount: TextView = view.findViewById(R.id.tvHistoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deposit_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val log = getItem(position)

        holder.tvGoalName.text = log.goalName

        // Format ngày giờ
        val dateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        holder.tvDate.text = log.transactionDate.format(dateFormatter)

        // Format tiền tệ
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvAmount.text = "+ ${currencyFormatter.format(log.amount)}"
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<DepositLog>() {
        override fun areItemsTheSame(oldItem: DepositLog, newItem: DepositLog) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DepositLog, newItem: DepositLog) = oldItem == newItem
    }
}