package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.MessPlan

class MessPlanAdapter(
    private val plans: List<MessPlan>,
    private val showCreator: Boolean = false
) : RecyclerView.Adapter<MessPlanAdapter.MessPlanViewHolder>() {

    class MessPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.planDateText)
        val breakfastText: TextView = itemView.findViewById(R.id.breakfastText)
        val lunchText: TextView = itemView.findViewById(R.id.lunchText)
        val snacksText: TextView = itemView.findViewById(R.id.snacksText)
        val dinnerText: TextView = itemView.findViewById(R.id.dinnerText)
        val notesText: TextView = itemView.findViewById(R.id.notesText)
        val metaText: TextView = itemView.findViewById(R.id.planMetaText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessPlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mess_plan, parent, false)
        return MessPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessPlanViewHolder, position: Int) {
        val plan = plans[position]
        holder.dateText.text = formatPlanDate(plan.planDate)
        holder.breakfastText.text = "Breakfast: ${plan.breakfast}"
        holder.lunchText.text = "Lunch: ${plan.lunch}"
        holder.snacksText.text = "Snacks: ${plan.eveningSnacks}"
        holder.dinnerText.text = "Dinner: ${plan.dinner}"

        if (plan.notes.isBlank()) {
            holder.notesText.visibility = View.GONE
        } else {
            holder.notesText.visibility = View.VISIBLE
            holder.notesText.text = "Notes: ${plan.notes}"
        }

        if (showCreator) {
            val creatorName = plan.createdBy?.name ?: "Admin"
            holder.metaText.visibility = View.VISIBLE
            holder.metaText.text = "Updated by $creatorName"
        } else {
            holder.metaText.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = plans.size

    private fun formatPlanDate(value: String): String {
        return value.substringBefore("T")
    }
}
