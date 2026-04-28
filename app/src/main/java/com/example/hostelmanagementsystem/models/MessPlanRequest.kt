package com.example.hostelmanagementsystem.models

data class MessPlanRequest(
    val adminId: String,
    val planDate: String,
    val breakfast: String,
    val lunch: String,
    val eveningSnacks: String,
    val dinner: String,
    val notes: String
)
