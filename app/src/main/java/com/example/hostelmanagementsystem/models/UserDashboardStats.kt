package com.example.hostelmanagementsystem.models

data class UserDashboardStats(
    val totalRooms: Int,
    val availableRooms: Int,
    val occupiedBeds: Int,
    val totalCapacity: Int
)
