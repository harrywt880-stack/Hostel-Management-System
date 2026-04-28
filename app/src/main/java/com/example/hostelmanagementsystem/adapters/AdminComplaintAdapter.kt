package com.example.hostelmanagementsystem.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Complaint

class AdminComplaintAdapter(
    private val complaints: List<Complaint>,
    private val onUpdateComplaint: (Complaint, String, String) -> Unit
) : RecyclerView.Adapter<AdminComplaintAdapter.AdminComplaintViewHolder>() {

    private val statuses = listOf("open", "accepted", "resolved", "closed")

    class AdminComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintId: TextView = itemView.findViewById(R.id.complaintIdText)
        val userName: TextView = itemView.findViewById(R.id.userNameText)
        val userEmail: TextView = itemView.findViewById(R.id.userEmailText)
        val complaintSubject: TextView = itemView.findViewById(R.id.complaintSubjectText)
        val complaintDescription: TextView = itemView.findViewById(R.id.complaintDescriptionText)
        val complaintMeta: TextView = itemView.findViewById(R.id.complaintMetaText)
        val currentStatus: TextView = itemView.findViewById(R.id.currentStatusText)
        val statusSelector: AutoCompleteTextView = itemView.findViewById(R.id.statusSelector)
        val adminNotesInput: EditText = itemView.findViewById(R.id.adminNotesInput)
        val updateBtn: Button = itemView.findViewById(R.id.updateComplaintBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminComplaintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_complaint, parent, false)
        return AdminComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminComplaintViewHolder, position: Int) {
        val complaint = complaints[position]
        val statusLabels = statuses.map { it.replaceFirstChar { ch -> ch.uppercase() }.replace("_", " ") }

        holder.complaintId.text = complaint.complaintId
        holder.userName.text = complaint.user?.name ?: "Unknown user"
        holder.userEmail.text = complaint.user?.email ?: ""
        holder.complaintSubject.text = complaint.subject
        holder.complaintDescription.text = complaint.description
        holder.complaintMeta.text = complaint.createdAt?.replace("T", " ")?.take(16) ?: "Recently created"
        holder.currentStatus.text = complaint.status.replace("_", " ").uppercase()
        holder.currentStatus.setTextColor(Color.parseColor(getStatusColor(complaint.status)))

        holder.statusSelector.setAdapter(
            ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_dropdown_item_1line,
                statusLabels
            )
        )
        holder.statusSelector.setText(
            complaint.status.replaceFirstChar { it.uppercase() }.replace("_", " "),
            false
        )
        holder.statusSelector.setOnClickListener {
            holder.statusSelector.showDropDown()
        }
        holder.statusSelector.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.statusSelector.showDropDown()
            }
        }
        holder.adminNotesInput.setText(complaint.adminNotes)

        holder.updateBtn.setOnClickListener {
            val selectedLabel = holder.statusSelector.text.toString().trim()
            val selectedStatus = statuses.firstOrNull {
                it.replace("_", " ").equals(selectedLabel, ignoreCase = true)
            } ?: complaint.status

            onUpdateComplaint(
                complaint,
                selectedStatus,
                holder.adminNotesInput.text.toString().trim()
            )
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
}
