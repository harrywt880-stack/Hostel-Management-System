package com.example.hostelmanagementsystem.models

data class Booking(
    val id: String,
    val status: String,
    val bookedAt: String?,
    val releasedAt: String?,
    val room: Room?
)
