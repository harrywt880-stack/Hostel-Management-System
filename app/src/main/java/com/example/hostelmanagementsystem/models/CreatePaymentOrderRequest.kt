package com.example.hostelmanagementsystem.models

data class CreatePaymentOrderRequest(
    val userId: String,
    val feeId: String?
)
