package com.example.hostelmanagementsystem.models

data class CreatePaymentOrderResponse(
    val message: String,
    val keyId: String,
    val orderId: String,
    val amount: Int,
    val currency: String,
    val fee: Fee,
    val user: PaymentUser
)

data class PaymentUser(
    val name: String,
    val email: String
)
