package com.example.hostelmanagementsystem.models

data class Room(
    val _id: String?,
    val roomNumber: String,
    val floor: String,
    val capacity: Int,
    val occupiedBeds: Int,
    val status: String
)
