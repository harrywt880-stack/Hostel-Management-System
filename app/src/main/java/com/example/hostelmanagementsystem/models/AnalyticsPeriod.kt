package com.example.hostelmanagementsystem.models

data class AnalyticsPeriod(
    val label: String,
    val startDate: String,
    val endDate: String,
    val newUsers: Int,
    val bookingCreated: Int,
    val bookingReleased: Int,
    val requestsSubmitted: Int,
    val requestsAssigned: Int,
    val complaintsRaised: Int,
    val complaintsResolved: Int,
    val visitorsLogged: Int,
    val noticesPublished: Int,
    val messPlansPublished: Int
)
