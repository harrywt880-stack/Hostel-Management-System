package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.RoomNotification

class NotificationAdapter(
    private val notifications: List<RoomNotification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationType: TextView = itemView.findViewById(R.id.notificationType)
        val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
        val notificationMeta: TextView = itemView.findViewById(R.id.notificationMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.notificationType.text = notification.type.replace("_", " ").uppercase()
        holder.notificationTitle.text = notification.title
        holder.notificationMessage.text = notification.message
        holder.notificationMeta.text = notification.createdAt.replace("T", " ").take(16)
    }

    override fun getItemCount(): Int = notifications.size
}
