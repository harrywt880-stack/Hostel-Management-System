package com.example.hostelmanagementsystem

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.AdminFeeAdapter
import com.example.hostelmanagementsystem.models.CreateFeeRequest
import com.example.hostelmanagementsystem.models.Fee
import com.example.hostelmanagementsystem.models.FeeResponse
import com.example.hostelmanagementsystem.models.UpdateFeeRequest
import com.example.hostelmanagementsystem.models.User
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SocketManager
import com.example.hostelmanagementsystem.ui.UiEffects
import com.google.gson.Gson
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminPaymentsActivity : AppCompatActivity() {

    private lateinit var socket: Socket

    private lateinit var backText: TextView
    private lateinit var studentSpinner: Spinner
    private lateinit var feeTitleInput: EditText
    private lateinit var monthLabelInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var dueDateInput: EditText
    private lateinit var adminNoteInput: EditText
    private lateinit var createFeeBtn: Button
    private lateinit var feesRecyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var loadingText: TextView

    private val gson = Gson()
    private var students: List<User> = emptyList()
    private var fees: List<Fee> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payments)
        UiEffects.animateScreenIn(this)

        socket = SocketManager.getSocket()

        backText = findViewById(R.id.backText)
        studentSpinner = findViewById(R.id.studentSpinner)
        feeTitleInput = findViewById(R.id.feeTitleInput)
        monthLabelInput = findViewById(R.id.monthLabelInput)
        amountInput = findViewById(R.id.amountInput)
        dueDateInput = findViewById(R.id.dueDateInput)
        adminNoteInput = findViewById(R.id.adminNoteInput)
        createFeeBtn = findViewById(R.id.createFeeBtn)
        feesRecyclerView = findViewById(R.id.feesRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        loadingText = findViewById(R.id.loadingText)

        feesRecyclerView.layoutManager = LinearLayoutManager(this)
        feesRecyclerView.isNestedScrollingEnabled = false

        backText.setOnClickListener { finish() }
        createFeeBtn.setOnClickListener {
            UiEffects.pulse(createFeeBtn)
            createFee()
        }
    }

    override fun onStart() {
        super.onStart()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchStudents()
        fetchFees()
    }

    override fun onStop() {
        super.onStop()
        socket.off("adminPaymentsUpdated")
        socket.disconnect()
    }

    private fun bindRealtimeEvents() {
        socket.off("adminPaymentsUpdated")
        socket.on("adminPaymentsUpdated") {
            runOnUiThread { fetchFees(silent = true) }
        }
    }

    private fun fetchStudents() {
        studentSpinner.isEnabled = false

        ApiClient.apiService.getUsers()
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful && response.body() != null) {
                        students = response.body().orEmpty().filter { it.role.equals("student", ignoreCase = true) }
                        bindStudentSpinner()
                    } else {
                        Toast.makeText(
                            this@AdminPaymentsActivity,
                            "Failed to load students",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(
                        this@AdminPaymentsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun bindStudentSpinner() {
        val labels = if (students.isEmpty()) {
            listOf("No students available")
        } else {
            students.map { "${it.name} (${it.email})" }
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        studentSpinner.adapter = adapter
        studentSpinner.isEnabled = students.isNotEmpty()
    }

    private fun fetchFees(silent: Boolean = false) {
        if (!silent) {
            loadingText.text = "Loading fees..."
        }

        ApiClient.apiService.getAllFees()
            .enqueue(object : Callback<List<Fee>> {
                override fun onResponse(call: Call<List<Fee>>, response: Response<List<Fee>>) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        fees = response.body().orEmpty()
                        emptyText.text = if (fees.isEmpty()) "No fees created yet" else ""
                        feesRecyclerView.adapter = AdminFeeAdapter(
                            fees = fees,
                            onUpdateStatus = ::updateFeeStatus
                        )
                    } else {
                        Toast.makeText(
                            this@AdminPaymentsActivity,
                            "Failed to load fees",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Fee>>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@AdminPaymentsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun createFee() {
        if (students.isEmpty()) {
            Toast.makeText(this, "No student available for fee assignment", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = studentSpinner.selectedItemPosition
        val student = students.getOrNull(selectedIndex)

        if (student == null) {
            Toast.makeText(this, "Select a student first", Toast.LENGTH_SHORT).show()
            return
        }

        val title = feeTitleInput.text.toString().trim()
        val monthLabel = monthLabelInput.text.toString().trim()
        val amountText = amountInput.text.toString().trim()
        val dueDate = dueDateInput.text.toString().trim().ifBlank { null }
        val adminNote = adminNoteInput.text.toString().trim()

        if (title.isBlank()) {
            feeTitleInput.error = "Fee title required"
            return
        }

        if (monthLabel.isBlank()) {
            monthLabelInput.error = "Month label required"
            return
        }

        if (amountText.isBlank()) {
            amountInput.error = "Amount required"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountInput.error = "Enter a valid amount"
            return
        }

        createFeeBtn.isEnabled = false
        createFeeBtn.text = "Creating..."

        ApiClient.apiService.createFee(
            CreateFeeRequest(
                userId = student.id,
                title = title,
                monthLabel = monthLabel,
                amount = amount,
                dueDate = dueDate
            )
        ).enqueue(object : Callback<FeeResponse> {
            override fun onResponse(call: Call<FeeResponse>, response: Response<FeeResponse>) {
                createFeeBtn.isEnabled = true
                createFeeBtn.text = "Create Fee"

                if (response.isSuccessful && response.body()?.fee != null) {
                    val createdFee = response.body()?.fee
                    if (!adminNote.isBlank() && createdFee != null) {
                        updateFeeStatus(createdFee, createdFee.status, adminNote, dueDate)
                    } else {
                        clearCreateForm()
                        Toast.makeText(
                            this@AdminPaymentsActivity,
                            response.body()?.message ?: "Fee created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchFees(silent = true)
                    }
                } else {
                    Toast.makeText(
                        this@AdminPaymentsActivity,
                        extractErrorMessage(response, "Unable to create fee"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FeeResponse>, t: Throwable) {
                createFeeBtn.isEnabled = true
                createFeeBtn.text = "Create Fee"
                Toast.makeText(
                    this@AdminPaymentsActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun updateFeeStatus(
        fee: Fee,
        status: String,
        adminNoteOverride: String? = null,
        dueDateOverride: String? = null
    ) {
        ApiClient.apiService.updateFee(
            fee.id,
            UpdateFeeRequest(
                status = status,
                adminNote = adminNoteOverride ?: fee.adminNote,
                dueDate = dueDateOverride ?: fee.dueDate
            )
        ).enqueue(object : Callback<FeeResponse> {
            override fun onResponse(call: Call<FeeResponse>, response: Response<FeeResponse>) {
                if (response.isSuccessful) {
                    if (adminNoteOverride != null) {
                        clearCreateForm()
                    }
                    Toast.makeText(
                        this@AdminPaymentsActivity,
                        response.body()?.message ?: "Fee updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchFees(silent = true)
                } else {
                    Toast.makeText(
                        this@AdminPaymentsActivity,
                        extractErrorMessage(response, "Unable to update fee"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FeeResponse>, t: Throwable) {
                Toast.makeText(
                    this@AdminPaymentsActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun clearCreateForm() {
        feeTitleInput.text?.clear()
        monthLabelInput.text?.clear()
        amountInput.text?.clear()
        dueDateInput.text?.clear()
        adminNoteInput.text?.clear()
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
