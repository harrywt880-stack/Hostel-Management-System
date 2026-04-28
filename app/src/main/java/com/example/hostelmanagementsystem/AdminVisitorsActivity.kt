package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.VisitorAdapter
import com.example.hostelmanagementsystem.models.User
import com.example.hostelmanagementsystem.models.Visitor
import com.example.hostelmanagementsystem.models.VisitorRequest
import com.example.hostelmanagementsystem.models.VisitorResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.network.SocketManager
import com.example.hostelmanagementsystem.ui.UiEffects
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminVisitorsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var socket: Socket

    private lateinit var backText: TextView
    private lateinit var userInput: AutoCompleteTextView
    private lateinit var visitorNameInput: EditText
    private lateinit var visitorPhoneInput: EditText
    private lateinit var relationInput: EditText
    private lateinit var purposeInput: EditText
    private lateinit var visitDateInput: EditText
    private lateinit var saveVisitorBtn: Button
    private lateinit var loadingText: TextView
    private lateinit var emptyText: TextView
    private lateinit var visitorsRecyclerView: RecyclerView

    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_visitors)
        UiEffects.animateScreenIn(this)

        sessionManager = SessionManager(this)
        socket = SocketManager.getSocket()

        backText = findViewById(R.id.backText)
        userInput = findViewById(R.id.userInput)
        visitorNameInput = findViewById(R.id.visitorNameInput)
        visitorPhoneInput = findViewById(R.id.visitorPhoneInput)
        relationInput = findViewById(R.id.relationInput)
        purposeInput = findViewById(R.id.purposeInput)
        visitDateInput = findViewById(R.id.visitDateInput)
        saveVisitorBtn = findViewById(R.id.saveVisitorBtn)
        loadingText = findViewById(R.id.loadingText)
        emptyText = findViewById(R.id.emptyText)
        visitorsRecyclerView = findViewById(R.id.visitorsRecyclerView)

        visitorsRecyclerView.layoutManager = LinearLayoutManager(this)
        visitorsRecyclerView.isNestedScrollingEnabled = false

        backText.setOnClickListener { finish() }
        userInput.setOnClickListener { userInput.showDropDown() }
        userInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                userInput.showDropDown()
            }
        }
        saveVisitorBtn.setOnClickListener {
            UiEffects.pulse(saveVisitorBtn)
            saveVisitor()
        }
    }

    override fun onStart() {
        super.onStart()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchUsers()
        fetchVisitors()
    }

    override fun onStop() {
        super.onStop()
        socket.off("visitorsUpdated")
        socket.disconnect()
    }

    private fun bindRealtimeEvents() {
        socket.off("visitorsUpdated")
        socket.on("visitorsUpdated") {
            runOnUiThread { fetchVisitors(silent = true) }
        }
    }

    private fun fetchUsers() {
        ApiClient.apiService.getUsers()
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful && response.body() != null) {
                        users = response.body().orEmpty().filter { it.role != "admin" }
                        bindUserOptions(users)
                    } else {
                        Toast.makeText(
                            this@AdminVisitorsActivity,
                            "Failed to load users",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(
                        this@AdminVisitorsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun bindUserOptions(users: List<User>) {
        val labels = users.map { labelForUser(it) }
        userInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                labels
            )
        )
    }

    private fun fetchVisitors(silent: Boolean = false) {
        if (!silent) {
            loadingText.text = "Loading visitor entries..."
        }

        ApiClient.apiService.getVisitors()
            .enqueue(object : Callback<List<Visitor>> {
                override fun onResponse(call: Call<List<Visitor>>, response: Response<List<Visitor>>) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        val visitors = response.body().orEmpty()
                        emptyText.text = if (visitors.isEmpty()) "No visitor entries yet" else ""
                        visitorsRecyclerView.adapter = VisitorAdapter(visitors)
                    } else {
                        Toast.makeText(
                            this@AdminVisitorsActivity,
                            "Failed to load visitors",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Visitor>>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@AdminVisitorsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun saveVisitor() {
        val userLabel = userInput.text.toString().trim()
        val selectedUser = users.firstOrNull { labelForUser(it) == userLabel }
        val visitorName = visitorNameInput.text.toString().trim()
        val visitorPhone = visitorPhoneInput.text.toString().trim()
        val relation = relationInput.text.toString().trim()
        val purpose = purposeInput.text.toString().trim()
        val visitDate = visitDateInput.text.toString().trim()

        if (selectedUser == null) {
            userInput.error = "Choose a user"
            return
        }

        if (visitorName.isBlank()) {
            visitorNameInput.error = "Visitor name required"
            return
        }

        if (visitorPhone.isBlank()) {
            visitorPhoneInput.error = "Phone required"
            return
        }

        if (relation.isBlank()) {
            relationInput.error = "Relation required"
            return
        }

        if (purpose.isBlank()) {
            purposeInput.error = "Purpose required"
            return
        }

        if (visitDate.isBlank()) {
            visitDateInput.error = "Use format YYYY-MM-DD"
            return
        }

        saveVisitorBtn.isEnabled = false
        saveVisitorBtn.text = "Saving..."

        ApiClient.apiService.createVisitor(
            VisitorRequest(
                adminId = sessionManager.getUserId(),
                userId = selectedUser.id,
                visitorName = visitorName,
                visitorPhone = visitorPhone,
                relationToUser = relation,
                purpose = purpose,
                visitDate = "${visitDate}T00:00:00.000Z"
            )
        ).enqueue(object : Callback<VisitorResponse> {
            override fun onResponse(call: Call<VisitorResponse>, response: Response<VisitorResponse>) {
                saveVisitorBtn.isEnabled = true
                saveVisitorBtn.text = "Save Visitor Entry"

                if (response.isSuccessful) {
                    clearForm()
                    Toast.makeText(
                        this@AdminVisitorsActivity,
                        response.body()?.message ?: "Visitor added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchVisitors(silent = true)
                } else {
                    Toast.makeText(
                        this@AdminVisitorsActivity,
                        "Unable to save visitor entry",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<VisitorResponse>, t: Throwable) {
                saveVisitorBtn.isEnabled = true
                saveVisitorBtn.text = "Save Visitor Entry"
                Toast.makeText(
                    this@AdminVisitorsActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun clearForm() {
        userInput.text?.clear()
        visitorNameInput.text?.clear()
        visitorPhoneInput.text?.clear()
        relationInput.text?.clear()
        purposeInput.text?.clear()
        visitDateInput.text?.clear()
    }

    private fun labelForUser(user: User): String {
        return "${user.name} | ${user.email} | ${user.role}"
    }
}
