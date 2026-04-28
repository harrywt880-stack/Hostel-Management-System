package com.example.hostelmanagementsystem.models

data class RoomRequestItem(
    val id: String,
    val status: String,
    val createdAt: String?,
    val assignedAt: String?,
    val cancelledAt: String?,
    val user: User?,
    val preferredRoom: Room?,
    val assignedRoom: Room?
)
