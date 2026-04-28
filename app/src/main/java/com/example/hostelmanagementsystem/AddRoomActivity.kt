package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.Room
import com.example.hostelmanagementsystem.models.RoomResponse
import com.example.hostelmanagementsystem.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRoomActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var roomNumberInput: EditText
    private lateinit var floorInput: EditText
    private lateinit var capacityInput: EditText
    private lateinit var occupiedBedsInput: EditText
    private lateinit var addRoomBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_room)

        backText = findViewById(R.id.backText)
        roomNumberInput = findViewById(R.id.roomNumberInput)
        floorInput = findViewById(R.id.floorInput)
        capacityInput = findViewById(R.id.capacityInput)
        occupiedBedsInput = findViewById(R.id.occupiedBedsInput)
        addRoomBtn = findViewById(R.id.addRoomBtn)

        backText.setOnClickListener { finish() }

        addRoomBtn.setOnClickListener {
            addRoom()
        }
    }

    private fun addRoom() {
        val roomNumber = roomNumberInput.text.toString().trim()
        val floor = floorInput.text.toString().trim()
        val capacityText = capacityInput.text.toString().trim()
        val occupiedText = occupiedBedsInput.text.toString().trim()

        if (roomNumber.isEmpty()) {
            roomNumberInput.error = "Room number required"
            return
        }

        if (floor.isEmpty()) {
            floorInput.error = "Floor required"
            return
        }

        if (capacityText.isEmpty()) {
            capacityInput.error = "Capacity required"
            return
        }

        val capacity = capacityText.toInt()
        val occupiedBeds = if (occupiedText.isEmpty()) 0 else occupiedText.toInt()

        if (occupiedBeds > capacity) {
            occupiedBedsInput.error = "Occupied beds cannot be more than capacity"
            return
        }

        val status = if (occupiedBeds >= capacity) "full" else "available"

        val room = Room(
            _id = null,
            roomNumber = roomNumber,
            floor = floor,
            capacity = capacity,
            occupiedBeds = occupiedBeds,
            status = status
        )

        addRoomBtn.isEnabled = false
        addRoomBtn.text = "Adding..."

        ApiClient.apiService.addRoom(room)
            .enqueue(object : Callback<RoomResponse> {
                override fun onResponse(
                    call: Call<RoomResponse>,
                    response: Response<RoomResponse>
                ) {
                    addRoomBtn.isEnabled = true
                    addRoomBtn.text = "Add Room"

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddRoomActivity,
                            "Room added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddRoomActivity,
                            "Failed to add room",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                    addRoomBtn.isEnabled = true
                    addRoomBtn.text = "Add Room"
                    Toast.makeText(
                        this@AddRoomActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
