package com.example.hostelmanagementsystem.models

data class MessPlan(
    val id: String,
    val planDate: String,
    val breakfast: String,
    val lunch: String,
    val eveningSnacks: String,
    val dinner: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: User?
)
