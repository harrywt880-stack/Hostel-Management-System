package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Visitor

class VisitorAdapter(
    private val visitors: List<Visitor>
) : RecyclerView.Adapter<VisitorAdapter.VisitorViewHolder>() {

    class VisitorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val visitorTitle: TextView = itemView.findViewById(R.id.visitorTitle)
        val visitorMeta: TextView = itemView.findViewById(R.id.visitorMeta)
        val visitorPurpose: TextView = itemView.findViewById(R.id.visitorPurpose)
        val visitorDate: TextView = itemView.findViewById(R.id.visitorDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visitor, parent, false)
        return VisitorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        val visitor = visitors[position]
        val user = visitor.user
        val ownerName = user?.name ?: "Unknown user"
        val ownerRole = user?.role?.replaceFirstChar { it.uppercase() } ?: "User"

        holder.visitorTitle.text = "${visitor.visitorName} visiting $ownerName"
        holder.visitorMeta.text = "${visitor.relationToUser} | ${visitor.visitorPhone} | $ownerRole"
        holder.visitorPurpose.text = "Purpose: ${visitor.purpose}"
        holder.visitorDate.text = "Visit date: ${formatDate(visitor.visitDate)}"
    }

    override fun getItemCount(): Int = visitors.size

    private fun formatDate(value: String): String {
        return value.replace("T", " ").substringBefore(".")
    }
}
