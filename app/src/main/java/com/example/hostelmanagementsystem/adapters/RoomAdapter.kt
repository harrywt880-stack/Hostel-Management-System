package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Room

class RoomAdapter(
    private val rooms: List<Room>
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNumber: TextView = itemView.findViewById(R.id.roomNumber)
        val roomFloor: TextView = itemView.findViewById(R.id.roomFloor)
        val roomBeds: TextView = itemView.findViewById(R.id.roomBeds)
        val roomStatus: TextView = itemView.findViewById(R.id.roomStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]

        holder.roomNumber.text = "Room ${room.roomNumber}"
        holder.roomFloor.text = room.floor
        holder.roomBeds.text = "${room.occupiedBeds}/${room.capacity} beds occupied"
        holder.roomStatus.text = room.status.uppercase()
    }

    override fun getItemCount(): Int = rooms.size
}
