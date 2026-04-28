package com.example.hostelmanagementsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.R
import com.example.hostelmanagementsystem.models.Room
import com.example.hostelmanagementsystem.models.RoomRequestItem

class AdminRequestAdapter(
    private val requests: List<RoomRequestItem>,
    private val availableRooms: List<Room>,
    private val onAssignRoom: (RoomRequestItem, Room) -> Unit
) : RecyclerView.Adapter<AdminRequestAdapter.AdminRequestViewHolder>() {

    class AdminRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentName: TextView = itemView.findViewById(R.id.studentName)
        val studentEmail: TextView = itemView.findViewById(R.id.studentEmail)
        val preferredRoom: TextView = itemView.findViewById(R.id.preferredRoom)
        val roomSelector: AutoCompleteTextView = itemView.findViewById(R.id.roomSelector)
        val assignBtn: Button = itemView.findViewById(R.id.assignBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_request, parent, false)
        return AdminRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminRequestViewHolder, position: Int) {
        val request = requests[position]
        val roomLabels = availableRooms.map { "Room ${it.roomNumber} | Floor ${it.floor} | ${it.occupiedBeds}/${it.capacity}" }

        holder.studentName.text = request.user?.name ?: "Unknown student"
        holder.studentEmail.text = request.user?.email ?: ""
        holder.preferredRoom.text = request.preferredRoom?.let {
            "Preferred: Room ${it.roomNumber} | Floor ${it.floor}"
        } ?: "Preferred: Not selected"

        holder.roomSelector.setAdapter(
            ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_dropdown_item_1line,
                roomLabels
            )
        )

        val preferredIndex = availableRooms.indexOfFirst { it._id == request.preferredRoom?._id }
        if (preferredIndex >= 0) {
            holder.roomSelector.setText(roomLabels[preferredIndex], false)
        } else if (roomLabels.isNotEmpty()) {
            holder.roomSelector.setText(roomLabels.first(), false)
        } else {
            holder.roomSelector.setText("", false)
        }

        holder.assignBtn.isEnabled = availableRooms.isNotEmpty()
        holder.assignBtn.setOnClickListener {
            val selectedLabel = holder.roomSelector.text.toString()
            val selectedIndex = roomLabels.indexOf(selectedLabel).takeIf { it >= 0 } ?: 0
            val selectedRoom = availableRooms.getOrNull(selectedIndex)

            if (selectedRoom != null) {
                onAssignRoom(request, selectedRoom)
            }
        }
    }

    override fun getItemCount(): Int = requests.size
}
