package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Notice

class NoticeAdapter(
    private val notices: List<Notice>
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.noticeTitle)
        val messageText: TextView = itemView.findViewById(R.id.noticeMessage)
        val metaText: TextView = itemView.findViewById(R.id.noticeMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]
        val authorName = notice.createdBy?.name ?: "Admin"

        holder.titleText.text = notice.title
        holder.messageText.text = notice.message
        holder.metaText.text = "By $authorName • Sent to ${notice.audienceCount} users"
    }

    override fun getItemCount(): Int = notices.size
}
