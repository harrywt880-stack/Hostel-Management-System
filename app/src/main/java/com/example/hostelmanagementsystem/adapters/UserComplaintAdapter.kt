package com.example.hostelmanagementsystem.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Complaint

class UserComplaintAdapter(
    private val complaints: List<Complaint>
) : RecyclerView.Adapter<UserComplaintAdapter.UserComplaintViewHolder>() {

    class UserComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintId: TextView = itemView.findViewById(R.id.complaintIdText)
        val complaintSubject: TextView = itemView.findViewById(R.id.complaintSubjectText)
        val complaintStatus: TextView = itemView.findViewById(R.id.complaintStatusText)
        val complaintDescription: TextView = itemView.findViewById(R.id.complaintDescriptionText)
        val complaintMeta: TextView = itemView.findViewById(R.id.complaintMetaText)
        val adminNotes: TextView = itemView.findViewById(R.id.adminNotesText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserComplaintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_complaint, parent, false)
        return UserComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserComplaintViewHolder, position: Int) {
        val complaint = complaints[position]

        holder.complaintId.text = complaint.complaintId
        holder.complaintSubject.text = complaint.subject
        holder.complaintDescription.text = complaint.description
        val statusLabel = complaint.status.replace("_", " ").uppercase()
        val updatedLabel = complaint.updatedAt?.replace("T", " ")?.take(16) ?: "Recently updated"
        holder.complaintMeta.text = "$statusLabel | $updatedLabel"
        holder.complaintStatus.text = statusLabel
        holder.complaintStatus.setTextColor(Color.parseColor(getStatusColor(complaint.status)))
        applyStatusBadge(holder.complaintStatus, complaint.status)

        if (complaint.adminNotes.isBlank()) {
            holder.adminNotes.visibility = View.GONE
        } else {
            holder.adminNotes.visibility = View.VISIBLE
            holder.adminNotes.text = "Admin notes: ${complaint.adminNotes}"
        }
    }

    override fun getItemCount(): Int = complaints.size

    private fun getStatusColor(status: String): String {
        return when (status) {
            "resolved" -> "#15803D"
            "closed" -> "#0F172A"
            "accepted" -> "#2563EB"
            "in_progress" -> "#B45309"
            else -> "#DC2626"
        }
    }

    private fun getStatusBackground(status: String): String {
        return when (status) {
            "resolved" -> "#E9FBEF"
            "closed" -> "#E2E8F0"
            "accepted" -> "#E8F0FF"
            "in_progress" -> "#FFF4E5"
            else -> "#FEECEC"
        }
    }

    private fun applyStatusBadge(view: TextView, status: String) {
        val background = view.background
        if (background is GradientDrawable) {
            background.setColor(Color.parseColor(getStatusBackground(status)))
        }
    }
}
