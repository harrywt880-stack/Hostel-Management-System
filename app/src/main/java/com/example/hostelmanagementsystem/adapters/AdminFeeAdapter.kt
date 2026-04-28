package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Fee
import java.util.Locale

class AdminFeeAdapter(
    private val fees: List<Fee>,
    private val onUpdateStatus: (Fee, String) -> Unit
) : RecyclerView.Adapter<AdminFeeAdapter.AdminFeeViewHolder>() {

    class AdminFeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentName: TextView = itemView.findViewById(R.id.studentNameText)
        val studentEmail: TextView = itemView.findViewById(R.id.studentEmailText)
        val feeTitle: TextView = itemView.findViewById(R.id.feeTitleText)
        val feeMeta: TextView = itemView.findViewById(R.id.feeMetaText)
        val feeStatus: TextView = itemView.findViewById(R.id.feeStatusText)
        val feeNote: TextView = itemView.findViewById(R.id.feeNoteText)
        val markPaidBtn: Button = itemView.findViewById(R.id.markPaidBtn)
        val markPendingBtn: Button = itemView.findViewById(R.id.markPendingBtn)
        val markFailedBtn: Button = itemView.findViewById(R.id.markFailedBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_fee, parent, false)
        return AdminFeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminFeeViewHolder, position: Int) {
        val fee = fees[position]
        val student = fee.user

        holder.studentName.text = student?.name ?: "Student unavailable"
        holder.studentEmail.text = student?.email ?: "No email"
        holder.feeTitle.text = "${fee.title} (${fee.monthLabel})"
        holder.feeMeta.text =
            "Amount: ${formatAmount(fee.amount)} ${fee.currency} | Due: ${fee.dueDate ?: "Not set"}"
        holder.feeStatus.text = "Status: ${fee.status.uppercase()} | Source: ${(fee.paidSource ?: "razorpay").uppercase()}"
        holder.feeNote.text = if (fee.adminNote.isNullOrBlank()) {
            "No admin note added"
        } else {
            "Note: ${fee.adminNote}"
        }

        holder.markPaidBtn.isEnabled = !fee.status.equals("paid", ignoreCase = true)
        holder.markPendingBtn.isEnabled = !fee.status.equals("pending", ignoreCase = true)
        holder.markFailedBtn.isEnabled = !fee.status.equals("failed", ignoreCase = true)

        holder.markPaidBtn.setOnClickListener { onUpdateStatus(fee, "paid") }
        holder.markPendingBtn.setOnClickListener { onUpdateStatus(fee, "pending") }
        holder.markFailedBtn.setOnClickListener { onUpdateStatus(fee, "failed") }
    }

    override fun getItemCount(): Int = fees.size

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            "Rs ${amount.toInt()}"
        } else {
            String.format(Locale.US, "Rs %.2f", amount)
        }
    }
}
