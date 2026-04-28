package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.NoticeAdapter
import com.example.hostelmanagementsystem.models.Notice
import com.example.hostelmanagementsystem.models.NoticeRequest
import com.example.hostelmanagementsystem.models.NoticeResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.network.SocketManager
import com.example.hostelmanagementsystem.ui.UiEffects
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminNoticesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var socket: Socket

    private lateinit var backText: TextView
    private lateinit var titleInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var sendNoticeBtn: Button
    private lateinit var noticesRecyclerView: RecyclerView
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notices)
        UiEffects.animateScreenIn(this)

        sessionManager = SessionManager(this)
        socket = SocketManager.getSocket()

        backText = findViewById(R.id.backText)
        titleInput = findViewById(R.id.noticeTitleInput)
        messageInput = findViewById(R.id.noticeMessageInput)
        sendNoticeBtn = findViewById(R.id.sendNoticeBtn)
        noticesRecyclerView = findViewById(R.id.noticesRecyclerView)
        emptyText = findViewById(R.id.emptyText)

        noticesRecyclerView.layoutManager = LinearLayoutManager(this)
        noticesRecyclerView.isNestedScrollingEnabled = false

        backText.setOnClickListener { finish() }
        sendNoticeBtn.setOnClickListener {
            UiEffects.pulse(sendNoticeBtn)
            createNotice()
        }
    }

    override fun onStart() {
        super.onStart()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchNotices()
    }

    override fun onStop() {
        super.onStop()
        socket.off("noticesUpdated")
        socket.disconnect()
    }

    private fun bindRealtimeEvents() {
        socket.off("noticesUpdated")
        socket.on("noticesUpdated") {
            runOnUiThread { fetchNotices(silent = true) }
        }
    }

    private fun fetchNotices(silent: Boolean = false) {
        if (!silent) {
            emptyText.text = "Loading notices..."
        }

        ApiClient.apiService.getNotices()
            .enqueue(object : Callback<List<Notice>> {
                override fun onResponse(call: Call<List<Notice>>, response: Response<List<Notice>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val notices = response.body().orEmpty()
                        emptyText.text = if (notices.isEmpty()) "No notices announced yet" else ""
                        noticesRecyclerView.adapter = NoticeAdapter(notices)
                    } else {
                        emptyText.text = ""
                        Toast.makeText(
                            this@AdminNoticesActivity,
                            "Failed to load notices",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Notice>>, t: Throwable) {
                    emptyText.text = ""
                    Toast.makeText(
                        this@AdminNoticesActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun createNotice() {
        val title = titleInput.text.toString().trim()
        val message = messageInput.text.toString().trim()

        if (title.isBlank()) {
            titleInput.error = "Title required"
            return
        }

        if (message.isBlank()) {
            messageInput.error = "Message required"
            return
        }

        sendNoticeBtn.isEnabled = false
        sendNoticeBtn.text = "Sending..."

        ApiClient.apiService.createNotice(
            NoticeRequest(
                adminId = sessionManager.getUserId(),
                title = title,
                message = message
            )
        ).enqueue(object : Callback<NoticeResponse> {
            override fun onResponse(call: Call<NoticeResponse>, response: Response<NoticeResponse>) {
                sendNoticeBtn.isEnabled = true
                sendNoticeBtn.text = "Announce Notice"

                if (response.isSuccessful) {
                    titleInput.text?.clear()
                    messageInput.text?.clear()
                    Toast.makeText(
                        this@AdminNoticesActivity,
                        response.body()?.message ?: "Notice announced successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchNotices(silent = true)
                } else {
                    Toast.makeText(
                        this@AdminNoticesActivity,
                        "Unable to announce notice",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<NoticeResponse>, t: Throwable) {
                sendNoticeBtn.isEnabled = true
                sendNoticeBtn.text = "Announce Notice"
                Toast.makeText(
                    this@AdminNoticesActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
