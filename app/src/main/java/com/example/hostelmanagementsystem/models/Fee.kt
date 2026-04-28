package com.example.hostelmanagementsystem.models

data class Fee(
    val id: String,
    val userId: String?,
    val user: User?,
    val title: String,
    val monthLabel: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val dueDate: String?,
    val receipt: String?,
    val gateway: String,
    val gatewayOrderId: String?,
    val gatewayPaymentId: String?,
    val paidSource: String?,
    val adminNote: String?,
    val paidAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)
