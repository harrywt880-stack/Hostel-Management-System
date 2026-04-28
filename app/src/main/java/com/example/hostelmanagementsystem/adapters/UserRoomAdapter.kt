package com.example.hostelmanagementsystem.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Room

class UserRoomAdapter(
    private val rooms: List<Room>,
    private val currentBookedRoomId: String?,
    private val pendingRequestedRoomId: String?,
    private val selectedRoomId: String?,
    private val hasActiveBooking: Boolean,
    private val hasPendingRequest: Boolean,
    private val onSelectRoom: (Room) -> Unit
) : RecyclerView.Adapter<UserRoomAdapter.UserRoomViewHolder>() {

    class UserRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNumber: TextView = itemView.findViewById(R.id.roomNumber)
        val roomFloor: TextView = itemView.findViewById(R.id.roomFloor)
        val roomBeds: TextView = itemView.findViewById(R.id.roomBeds)
        val roomStatus: TextView = itemView.findViewById(R.id.roomStatus)
        val roomActionBtn: Button = itemView.findViewById(R.id.roomActionBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_room, parent, false)

        return UserRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserRoomViewHolder, position: Int) {
        val room = rooms[position]
        val isCurrentRoom = currentBookedRoomId != null && room._id == currentBookedRoomId
        val isPendingPreferredRoom = pendingRequestedRoomId != null && room._id == pendingRequestedRoomId
        val isSelectedRoom = selectedRoomId != null && room._id == selectedRoomId
        val isAvailable = room.occupiedBeds < room.capacity && room.status != "maintenance"

        holder.roomNumber.text = "Room ${room.roomNumber}"
        holder.roomFloor.text = "Floor ${room.floor}"
        holder.roomBeds.text = "${room.occupiedBeds}/${room.capacity} beds occupied"
        holder.roomStatus.text = room.status.uppercase()

        when {
            isCurrentRoom -> {
                holder.roomActionBtn.text = "Current Room"
                holder.roomActionBtn.isEnabled = false
                holder.roomActionBtn.setOnClickListener(null)
            }

            hasActiveBooking -> {
                holder.roomActionBtn.text = "Already Assigned"
                holder.roomActionBtn.isEnabled = false
                holder.roomActionBtn.setOnClickListener(null)
            }

            isPendingPreferredRoom -> {
                holder.roomActionBtn.text = "Requested"
                holder.roomActionBtn.isEnabled = false
                holder.roomActionBtn.setOnClickListener(null)
            }

            hasPendingRequest -> {
                holder.roomActionBtn.text = "Waiting For Approval"
                holder.roomActionBtn.isEnabled = false
                holder.roomActionBtn.setOnClickListener(null)
            }

            isAvailable -> {
                holder.roomActionBtn.text = if (isSelectedRoom) "Selected" else "Select Room"
                holder.roomActionBtn.isEnabled = true
                holder.roomActionBtn.setOnClickListener { onSelectRoom(room) }
            }

            else -> {
                holder.roomActionBtn.text = "Unavailable"
                holder.roomActionBtn.isEnabled = false
                holder.roomActionBtn.setOnClickListener(null)
            }
        }

        val textColor = when (room.status) {
            "full" -> "#DC2626"
            "maintenance" -> "#B45309"
            else -> "#2563EB"
        }
        holder.roomStatus.setTextColor(Color.parseColor(textColor))
        applyStatusBadge(holder.roomStatus, room.status)
    }

    override fun getItemCount(): Int = rooms.size

    private fun applyStatusBadge(view: TextView, status: String) {
        val fillColor = when (status) {
            "full" -> "#FEECEC"
            "maintenance" -> "#FFF4E5"
            else -> "#E8F0FF"
        }

        val background = view.background
        if (background is GradientDrawable) {
            background.setColor(Color.parseColor(fillColor))
        }
    }
}
