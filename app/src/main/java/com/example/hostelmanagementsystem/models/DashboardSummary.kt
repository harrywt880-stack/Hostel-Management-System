package com.example.hostelmanagementsystem.models

data class DashboardSummary(
    val totalUsers: Int,
    val totalAdmins: Int,
    val totalStudents: Int,
    val totalWardens: Int,
    val totalRooms: Int,
    val availableRooms: Int,
    val fullRooms: Int,
    val maintenanceRooms: Int,
    val totalCapacity: Int,
    val occupiedBeds: Int,
    val vacantBeds: Int,
    val occupancyRate: Int,
    val pendingRequests: Int,
    val openComplaints: Int
)
