package com.example.hostelmanagementsystem

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.DashboardSummary
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.ui.UiEffects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var dashboardContainer: LinearLayout
    private lateinit var dashboardGrid: GridLayout
    private lateinit var statsBanner: LinearLayout
    private lateinit var statsRow: LinearLayout
    private lateinit var logoutText: TextView
    private lateinit var welcomeText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var statStudents: TextView
    private lateinit var statRooms: TextView
    private lateinit var statPending: TextView
    private lateinit var sessionManager: SessionManager

    private lateinit var usersCard: TextView
    private lateinit var hostelCard: TextView
    private lateinit var roomsCard: TextView
    private lateinit var bookingsCard: TextView
    private lateinit var paymentsCard: TextView
    private lateinit var complaintsCard: TextView
    private lateinit var noticesCard: TextView
    private lateinit var attendanceCard: TextView
    private lateinit var visitorsCard: TextView
    private lateinit var messCard: TextView
    private lateinit var reportsCard: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        UiEffects.animateScreenIn(this)

        dashboardContainer = findViewById(R.id.dashboardContainer)
        dashboardGrid = findViewById(R.id.dashboardGrid)
        statsBanner = findViewById(R.id.statsBanner)
        statsRow = findViewById(R.id.statsRow)
        logoutText = findViewById(R.id.logoutText)
        welcomeText = findViewById(R.id.welcomeText)
        subtitleText = findViewById(R.id.subtitleText)
        statStudents = findViewById(R.id.statStudents)
        statRooms = findViewById(R.id.statRooms)
        statPending = findViewById(R.id.statPending)
        sessionManager = SessionManager(this)

        usersCard = findViewById(R.id.usersCard)
        hostelCard = findViewById(R.id.hostelCard)
        roomsCard = findViewById(R.id.roomsCard)
        bookingsCard = findViewById(R.id.bookingsCard)
        paymentsCard = findViewById(R.id.paymentsCard)
        complaintsCard = findViewById(R.id.complaintsCard)
        noticesCard = findViewById(R.id.noticesCard)
        attendanceCard = findViewById(R.id.attendanceCard)
        visitorsCard = findViewById(R.id.visitorsCard)
        messCard = findViewById(R.id.messCard)
        reportsCard = findViewById(R.id.reportsCard)

        welcomeText.text = "Welcome, ${sessionManager.getUserName()}"
        subtitleText.text = "Signed in as ${sessionManager.getUserRole().replaceFirstChar { it.uppercase() }}"

        animateDashboard()
        fetchDashboardSummary()

        usersCard.setOnClickListener {
            usersCard.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction {
                    usersCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .start()

                    val intent = android.content.Intent(this, UserListActivity::class.java)
                    startActivity(intent)
                }
                .start()
        }
        setCardClick(hostelCard, "Hostel Info Module")
        roomsCard.setOnClickListener {
            roomsCard.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction {
                    roomsCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .start()

                    val intent = android.content.Intent(this, RoomListActivity::class.java)
                    startActivity(intent)
                }
                .start()
        }
        bookingsCard.setOnClickListener {
            UiEffects.pulse(bookingsCard) {
                val intent = android.content.Intent(this, AdminBookingRequestsActivity::class.java)
                startActivity(intent)
            }
        }
        paymentsCard.setOnClickListener {
            UiEffects.pulse(paymentsCard) {
                val intent = android.content.Intent(this, AdminPaymentsActivity::class.java)
                startActivity(intent)
            }
        }
        complaintsCard.setOnClickListener {
            UiEffects.pulse(complaintsCard) {
                val intent = android.content.Intent(this, AdminComplaintsActivity::class.java)
                startActivity(intent)
            }
        }
        noticesCard.setOnClickListener {
            UiEffects.pulse(noticesCard) {
                val intent = android.content.Intent(this, AdminNoticesActivity::class.java)
                startActivity(intent)
            }
        }
        setCardClick(attendanceCard, "Attendance Module")
        visitorsCard.setOnClickListener {
            UiEffects.pulse(visitorsCard) {
                val intent = android.content.Intent(this, AdminVisitorsActivity::class.java)
                startActivity(intent)
            }
        }
        messCard.setOnClickListener {
            UiEffects.pulse(messCard) {
                val intent = android.content.Intent(this, AdminMessPlanActivity::class.java)
                startActivity(intent)
            }
        }
        reportsCard.setOnClickListener {
            UiEffects.pulse(reportsCard) {
                val intent = android.content.Intent(this, AdminAnalyticsActivity::class.java)
                startActivity(intent)
            }
        }

        logoutText.setOnClickListener {
            sessionManager.clearSession()
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardSummary()
    }

    private fun fetchDashboardSummary() {
        statStudents.text = "--\nStudents"
        statRooms.text = "--\nRooms"
        statPending.text = "--\nRequests"

        ApiClient.apiService.getDashboardSummary()
            .enqueue(object : Callback<DashboardSummary> {
                override fun onResponse(
                    call: Call<DashboardSummary>,
                    response: Response<DashboardSummary>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val summary = response.body()!!
                        statStudents.text = "${summary.totalStudents}\nStudents"
                        statRooms.text = "${summary.totalRooms}\nRooms"
                        statPending.text = "${summary.pendingRequests}\nRequests"
                    } else {
                        Toast.makeText(
                            this@DashboardActivity,
                            "Failed to load dashboard stats",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DashboardSummary>, t: Throwable) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun animateDashboard() {
        dashboardContainer.alpha = 0f
        dashboardContainer.translationY = 45f

        dashboardContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(240)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val cards = listOf(
            usersCard, hostelCard, roomsCard, bookingsCard, paymentsCard,
            complaintsCard, noticesCard, attendanceCard, visitorsCard, messCard, reportsCard
        )

        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.scaleX = 0.92f
            card.scaleY = 0.92f
            card.translationY = 60f

            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(220)
                .setStartDelay((index * 18).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun setCardClick(card: View, moduleName: String) {
        card.setOnClickListener {
            card.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction {
                    card.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .start()

                    val intent = android.content.Intent(this, ModuleActivity::class.java)
                    intent.putExtra("MODULE_TITLE", moduleName)
                    startActivity(intent)
                }
                .start()
        }
    }
}
