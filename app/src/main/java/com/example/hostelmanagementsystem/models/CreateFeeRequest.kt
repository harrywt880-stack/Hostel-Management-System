package com.example.hostelmanagementsystem.models

data class CreateFeeRequest(
    val userId: String,
    val title: String,
    val monthLabel: String,
    val amount: Double,
    val dueDate: String?
)
