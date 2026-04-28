package com.example.hostelmanagementsystem.models

data class UpdateFeeRequest(
    val status: String? = null,
    val adminNote: String? = null,
    val dueDate: String? = null
)
