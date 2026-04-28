package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.RoomAdapter
import com.example.hostelmanagementsystem.models.Room
import com.example.hostelmanagementsystem.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomListActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var addRoomBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingText: TextView
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        backText = findViewById(R.id.backText)
        addRoomBtn = findViewById(R.id.addRoomBtn)
        recyclerView = findViewById(R.id.roomsRecyclerView)
        loadingText = findViewById(R.id.loadingText)
        emptyText = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        backText.setOnClickListener { finish() }

        addRoomBtn.setOnClickListener {
            startActivity(Intent(this, AddRoomActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRooms()
    }

    private fun fetchRooms() {
        loadingText.text = "Loading rooms..."
        emptyText.text = ""

        ApiClient.apiService.getRooms()
            .enqueue(object : Callback<List<Room>> {
                override fun onResponse(
                    call: Call<List<Room>>,
                    response: Response<List<Room>>
                ) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        val rooms = response.body()!!

                        if (rooms.isEmpty()) {
                            emptyText.text = "No rooms found"
                            recyclerView.adapter = RoomAdapter(emptyList())
                        } else {
                            emptyText.text = ""
                            recyclerView.adapter = RoomAdapter(rooms)
                        }
                    } else {
                        Toast.makeText(
                            this@RoomListActivity,
                            "Failed to load rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@RoomListActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
