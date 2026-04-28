package com.example.hostelmanagementsystem.models

data class UserDashboardResponse(
    val user: User,
    val currentBooking: Booking?,
    val pendingRequest: RoomRequestItem?,
    val currentFee: Fee?,
    val feeHistory: List<Fee>,
    val stats: UserDashboardStats,
    val availableRooms: List<Room>,
    val rooms: List<Room>,
    val notifications: List<RoomNotification>,
    val complaints: List<Complaint>,
    val todaysMessPlan: MessPlan?,
    val messPlanHistory: List<MessPlan>
)
