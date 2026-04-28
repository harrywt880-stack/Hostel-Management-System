package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.MessPlanAdapter
import com.example.hostelmanagementsystem.models.MessPlan
import com.example.hostelmanagementsystem.models.MessPlanRequest
import com.example.hostelmanagementsystem.models.MessPlanResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.network.SocketManager
import com.example.hostelmanagementsystem.ui.UiEffects
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminMessPlanActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var socket: Socket

    private lateinit var backText: TextView
    private lateinit var planDateInput: EditText
    private lateinit var breakfastInput: EditText
    private lateinit var lunchInput: EditText
    private lateinit var snacksInput: EditText
    private lateinit var dinnerInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var savePlanBtn: Button
    private lateinit var emptyText: TextView
    private lateinit var historyRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_mess_plan)
        UiEffects.animateScreenIn(this)

        sessionManager = SessionManager(this)
        socket = SocketManager.getSocket()

        backText = findViewById(R.id.backText)
        planDateInput = findViewById(R.id.planDateInput)
        breakfastInput = findViewById(R.id.breakfastInput)
        lunchInput = findViewById(R.id.lunchInput)
        snacksInput = findViewById(R.id.snacksInput)
        dinnerInput = findViewById(R.id.dinnerInput)
        notesInput = findViewById(R.id.notesInput)
        savePlanBtn = findViewById(R.id.savePlanBtn)
        emptyText = findViewById(R.id.emptyText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.isNestedScrollingEnabled = false

        backText.setOnClickListener { finish() }
        savePlanBtn.setOnClickListener {
            UiEffects.pulse(savePlanBtn)
            savePlan()
        }
    }

    override fun onStart() {
        super.onStart()
        bindRealtimeEvents()
        if (!socket.connected()) {
            socket.connect()
        }
        fetchPlans()
    }

    override fun onStop() {
        super.onStop()
        socket.off("messPlansUpdated")
        socket.disconnect()
    }

    private fun bindRealtimeEvents() {
        socket.off("messPlansUpdated")
        socket.on("messPlansUpdated") {
            runOnUiThread { fetchPlans(silent = true) }
        }
    }

    private fun fetchPlans(silent: Boolean = false) {
        if (!silent) {
            emptyText.text = "Loading mess plans..."
        }

        ApiClient.apiService.getMessPlans()
            .enqueue(object : Callback<List<MessPlan>> {
                override fun onResponse(call: Call<List<MessPlan>>, response: Response<List<MessPlan>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val plans = response.body().orEmpty()
                        emptyText.text = if (plans.isEmpty()) "No mess plans saved yet" else ""
                        historyRecyclerView.adapter = MessPlanAdapter(plans, showCreator = true)
                    } else {
                        emptyText.text = ""
                        Toast.makeText(
                            this@AdminMessPlanActivity,
                            "Failed to load mess plans",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<MessPlan>>, t: Throwable) {
                    emptyText.text = ""
                    Toast.makeText(
                        this@AdminMessPlanActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun savePlan() {
        val planDate = planDateInput.text.toString().trim()
        val breakfast = breakfastInput.text.toString().trim()
        val lunch = lunchInput.text.toString().trim()
        val snacks = snacksInput.text.toString().trim()
        val dinner = dinnerInput.text.toString().trim()
        val notes = notesInput.text.toString().trim()

        if (planDate.isBlank()) {
            planDateInput.error = "Use format YYYY-MM-DD"
            return
        }

        if (breakfast.isBlank()) {
            breakfastInput.error = "Breakfast required"
            return
        }

        if (lunch.isBlank()) {
            lunchInput.error = "Lunch required"
            return
        }

        if (snacks.isBlank()) {
            snacksInput.error = "Snacks required"
            return
        }

        if (dinner.isBlank()) {
            dinnerInput.error = "Dinner required"
            return
        }

        savePlanBtn.isEnabled = false
        savePlanBtn.text = "Saving..."

        ApiClient.apiService.createOrUpdateMessPlan(
            MessPlanRequest(
                adminId = sessionManager.getUserId(),
                planDate = "${planDate}T00:00:00.000Z",
                breakfast = breakfast,
                lunch = lunch,
                eveningSnacks = snacks,
                dinner = dinner,
                notes = notes
            )
        ).enqueue(object : Callback<MessPlanResponse> {
            override fun onResponse(call: Call<MessPlanResponse>, response: Response<MessPlanResponse>) {
                savePlanBtn.isEnabled = true
                savePlanBtn.text = "Save Daily Mess Plan"

                if (response.isSuccessful) {
                    clearForm()
                    Toast.makeText(
                        this@AdminMessPlanActivity,
                        response.body()?.message ?: "Mess plan saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchPlans(silent = true)
                } else {
                    Toast.makeText(
                        this@AdminMessPlanActivity,
                        "Unable to save mess plan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MessPlanResponse>, t: Throwable) {
                savePlanBtn.isEnabled = true
                savePlanBtn.text = "Save Daily Mess Plan"
                Toast.makeText(
                    this@AdminMessPlanActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun clearForm() {
        planDateInput.text?.clear()
        breakfastInput.text?.clear()
        lunchInput.text?.clear()
        snacksInput.text?.clear()
        dinnerInput.text?.clear()
        notesInput.text?.clear()
    }
}
