package com.example.hostelmanagementsystem.models

data class AnalyticsSnapshot(
    val totalRooms: Int,
    val totalCapacity: Int,
    val occupiedBeds: Int,
    val activeBookings: Int,
    val pendingRequests: Int,
    val openComplaints: Int,
    val occupancyRate: Int
)
