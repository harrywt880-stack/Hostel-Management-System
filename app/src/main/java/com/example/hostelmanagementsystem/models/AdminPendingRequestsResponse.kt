package com.example.hostelmanagementsystem.models

data class AdminPendingRequestsResponse(
    val requests: List<RoomRequestItem>,
    val availableRooms: List<Room>
)
