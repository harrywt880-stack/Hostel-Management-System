package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.AnalyticsPeriod
import com.example.hostelmanagementsystem.models.AnalyticsResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.ui.UiEffects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminAnalyticsActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var loadingText: TextView
    private lateinit var generatedAtText: TextView

    private lateinit var snapshotOccupancyText: TextView
    private lateinit var snapshotBookingsText: TextView
    private lateinit var snapshotComplaintsText: TextView

    private lateinit var dailyTitle: TextView
    private lateinit var dailyBody: TextView
    private lateinit var weeklyTitle: TextView
    private lateinit var weeklyBody: TextView
    private lateinit var monthlyTitle: TextView
    private lateinit var monthlyBody: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytics)
        UiEffects.animateScreenIn(this)

        backText = findViewById(R.id.backText)
        loadingText = findViewById(R.id.loadingText)
        generatedAtText = findViewById(R.id.generatedAtText)
        snapshotOccupancyText = findViewById(R.id.snapshotOccupancyText)
        snapshotBookingsText = findViewById(R.id.snapshotBookingsText)
        snapshotComplaintsText = findViewById(R.id.snapshotComplaintsText)
        dailyTitle = findViewById(R.id.dailyTitle)
        dailyBody = findViewById(R.id.dailyBody)
        weeklyTitle = findViewById(R.id.weeklyTitle)
        weeklyBody = findViewById(R.id.weeklyBody)
        monthlyTitle = findViewById(R.id.monthlyTitle)
        monthlyBody = findViewById(R.id.monthlyBody)

        backText.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        fetchAnalytics()
    }

    private fun fetchAnalytics() {
        loadingText.text = "Loading analytics..."

        ApiClient.apiService.getAnalyticsReport()
            .enqueue(object : Callback<AnalyticsResponse> {
                override fun onResponse(
                    call: Call<AnalyticsResponse>,
                    response: Response<AnalyticsResponse>
                ) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        bindAnalytics(response.body()!!)
                    } else {
                        Toast.makeText(
                            this@AdminAnalyticsActivity,
                            "Failed to load analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AnalyticsResponse>, t: Throwable) {
                    loadingText.text = ""
                    Toast.makeText(
                        this@AdminAnalyticsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun bindAnalytics(data: AnalyticsResponse) {
        generatedAtText.text = "Generated: ${data.generatedAt.substringBefore(".").replace("T", " ")}"
        snapshotOccupancyText.text =
            "${data.snapshot.occupancyRate}% occupancy\n${data.snapshot.occupiedBeds}/${data.snapshot.totalCapacity} beds filled across ${data.snapshot.totalRooms} rooms"
        snapshotBookingsText.text =
            "${data.snapshot.activeBookings} active bookings\n${data.snapshot.pendingRequests} pending room requests"
        snapshotComplaintsText.text =
            "${data.snapshot.openComplaints} open complaints\nCurrent live hostel status"

        dailyTitle.text = buildTitle(data.daily)
        dailyBody.text = buildBody(data.daily)
        weeklyTitle.text = buildTitle(data.weekly)
        weeklyBody.text = buildBody(data.weekly)
        monthlyTitle.text = buildTitle(data.monthly)
        monthlyBody.text = buildBody(data.monthly)
    }

    private fun buildTitle(period: AnalyticsPeriod): String {
        return "${period.label} Report (${period.startDate.substringBefore("T")} to ${period.endDate.substringBefore("T")})"
    }

    private fun buildBody(period: AnalyticsPeriod): String {
        return "New users: ${period.newUsers}\n" +
            "Bookings created: ${period.bookingCreated}\n" +
            "Bookings released: ${period.bookingReleased}\n" +
            "Requests submitted: ${period.requestsSubmitted}\n" +
            "Requests assigned: ${period.requestsAssigned}\n" +
            "Complaints raised: ${period.complaintsRaised}\n" +
            "Complaints resolved: ${period.complaintsResolved}\n" +
            "Visitors logged: ${period.visitorsLogged}\n" +
            "Notices published: ${period.noticesPublished}\n" +
            "Mess plans published: ${period.messPlansPublished}"
    }
}
