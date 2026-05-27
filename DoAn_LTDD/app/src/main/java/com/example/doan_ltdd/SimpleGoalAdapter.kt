package com.example.doan_ltdd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SimpleGoalAdapter(
    private val onClick: (SavingsGoal) -> Unit
) : ListAdapter<SavingsGoal, SimpleGoalAdapter.ViewHolder>(GoalDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGoalNameSimple)
        val ivIcon: ImageView = view.findViewById(R.id.ivGoalIconSimple)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvName.text = item.name
        holder.ivIcon.setImageResource(item.iconResId)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    class GoalDiffCallback : DiffUtil.ItemCallback<SavingsGoal>() {
        override fun areItemsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal) = oldItem == newItem
    }
}