package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.AdminComplaintAdapter
import com.example.hostelmanagementsystem.models.Complaint
import com.example.hostelmanagementsystem.models.ComplaintResponse
import com.example.hostelmanagementsystem.models.ComplaintStatusUpdateRequest
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.network.SocketManager
import com.google.gson.Gson
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminComplaintsActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var complaintsRecyclerView: RecyclerView
    private lateinit var loadingText: TextView
    private lateinit var emptyText: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var socket: Socket

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_complaints)

        backText = findViewById(R.id.backText)
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView)
        loadingText = findViewById(R.id.loadingText)
        emptyText = findViewById(R.id.emptyText)
        sessionManager = SessionManager(this)
        socket = SocketManager.getSocket()

        complaintsRecyclerView.layoutManager = LinearLayoutManager(this)
        backText.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchComplaints()
    }

    override fun onPause() {
        super.onPause()
        socket.off("adminComplaintsUpdated")
    }

    private fun bindRealtimeEvents() {
        socket.off("adminComplaintsUpdated")
        socket.on("adminComplaintsUpdated") {
            runOnUiThread { fetchComplaints(silent = true) }
        }
    }

    private fun fetchComplaints(silent: Boolean = false) {
        if (!silent) {
            loadingText.text = "Loading complaints..."
        }

        ApiClient.apiService.getAdminComplaints()
            .enqueue(object : Callback<List<Complaint>> {
                override fun onResponse(call: Call<List<Complaint>>, response: Response<List<Complaint>>) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        val complaints = response.body()!!
                        emptyText.text = if (complaints.isEmpty()) "No complaints available" else ""
                        complaintsRecyclerView.adapter = AdminComplaintAdapter(
                            complaints = complaints,
                            onUpdateComplaint = ::updateComplaint
                        )
                    } else {
                        Toast.makeText(
                            this@AdminComplaintsActivity,
                            "Failed to load complaints",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Complaint>>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@AdminComplaintsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateComplaint(complaint: Complaint, status: String, adminNotes: String) {
        ApiClient.apiService.updateComplaintStatus(
            complaint.id,
            ComplaintStatusUpdateRequest(
                adminId = sessionManager.getUserId(),
                status = status,
                adminNotes = adminNotes
            )
        ).enqueue(object : Callback<ComplaintResponse> {
            override fun onResponse(call: Call<ComplaintResponse>, response: Response<ComplaintResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminComplaintsActivity,
                        "Complaint updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchComplaints(silent = true)
                } else {
                    Toast.makeText(
                        this@AdminComplaintsActivity,
                        extractErrorMessage(response, "Unable to update complaint"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ComplaintResponse>, t: Throwable) {
                Toast.makeText(
                    this@AdminComplaintsActivity,
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
