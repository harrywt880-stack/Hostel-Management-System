package com.example.hostelmanagementsystem.models

data class VerifyPaymentRequest(
    val feeId: String,
    val razorpay_order_id: String,
    val razorpay_payment_id: String,
    val razorpay_signature: String
)
