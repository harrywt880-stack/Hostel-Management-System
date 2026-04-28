package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.AdminRequestAdapter
import com.example.hostelmanagementsystem.models.AdminPendingRequestsResponse
import com.example.hostelmanagementsystem.models.AssignRoomRequest
import com.example.hostelmanagementsystem.models.Room
import com.example.hostelmanagementsystem.models.RoomRequestItem
import com.example.hostelmanagementsystem.models.RoomRequestResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.network.SocketManager
import com.google.gson.Gson
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminBookingRequestsActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var loadingText: TextView
    private lateinit var emptyText: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var socket: Socket

    private var availableRooms: List<Room> = emptyList()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_booking_requests)

        backText = findViewById(R.id.backText)
        requestsRecyclerView = findViewById(R.id.requestsRecyclerView)
        loadingText = findViewById(R.id.loadingText)
        emptyText = findViewById(R.id.emptyText)
        sessionManager = SessionManager(this)
        socket = SocketManager.getSocket()

        requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        backText.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchPendingRequests()
    }

    override fun onPause() {
        super.onPause()
        socket.off("adminRequestsUpdated")
        socket.off("roomsUpdated")
    }

    private fun bindRealtimeEvents() {
        socket.off("adminRequestsUpdated")
        socket.off("roomsUpdated")

        socket.on("adminRequestsUpdated") {
            runOnUiThread { fetchPendingRequests(silent = true) }
        }

        socket.on("roomsUpdated") {
            runOnUiThread { fetchPendingRequests(silent = true) }
        }
    }

    private fun fetchPendingRequests(silent: Boolean = false) {
        if (!silent) {
            loadingText.text = "Loading booking requests..."
        }

        ApiClient.apiService.getAdminPendingRequests()
            .enqueue(object : Callback<AdminPendingRequestsResponse> {
                override fun onResponse(
                    call: Call<AdminPendingRequestsResponse>,
                    response: Response<AdminPendingRequestsResponse>
                ) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        availableRooms = data.availableRooms

                        if (data.requests.isEmpty()) {
                            emptyText.text = "No pending booking requests"
                            requestsRecyclerView.adapter = AdminRequestAdapter(emptyList(), availableRooms) { _, _ -> }
                        } else {
                            emptyText.text = ""
                            requestsRecyclerView.adapter = AdminRequestAdapter(
                                data.requests,
                                availableRooms,
                                ::assignRoom
                            )
                        }
                    } else {
                        Toast.makeText(
                            this@AdminBookingRequestsActivity,
                            "Failed to load requests",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AdminPendingRequestsResponse>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@AdminBookingRequestsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun assignRoom(request: RoomRequestItem, room: Room) {
        ApiClient.apiService.assignRoomToRequest(
            request.id,
            AssignRoomRequest(
                adminId = sessionManager.getUserId(),
                roomId = room._id ?: return
            )
        ).enqueue(object : Callback<RoomRequestResponse> {
            override fun onResponse(call: Call<RoomRequestResponse>, response: Response<RoomRequestResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminBookingRequestsActivity,
                        "Room assigned successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchPendingRequests()
                } else {
                    Toast.makeText(
                        this@AdminBookingRequestsActivity,
                        extractErrorMessage(response, "Unable to assign room"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RoomRequestResponse>, t: Throwable) {
                Toast.makeText(
                    this@AdminBookingRequestsActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun extractErrorMessage(response: Response<*>, fallback: String): String {
        return try {
            val rawBody = response.errorBody()?.string()
            if (rawBody.isNullOrBlank()) {
                fallback
            } else {
                gson.fromJson(rawBody, ApiErrorResponse::class.java)?.message ?: fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    data class ApiErrorResponse(
        val message: String?
    )
}
