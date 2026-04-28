package com.example.hostelmanagementsystem.models

data class ComplaintRequest(
    val userId: String,
    val subject: String,
    val description: String
)
