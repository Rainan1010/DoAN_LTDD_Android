package com.example.doan_ltdd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class NotificationAdapter : ListAdapter<NotificationLog, NotificationAdapter.NotiViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotiTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotiMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotiTime)
        private val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")

        fun bind(item: NotificationLog) {
            tvTitle.text = item.title
            tvMessage.text = item.message
            tvTime.text = item.timestamp.format(formatter)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationLog>() {
        override fun areItemsTheSame(oldItem: NotificationLog, newItem: NotificationLog) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NotificationLog, newItem: NotificationLog) = oldItem == newItem
    }
}