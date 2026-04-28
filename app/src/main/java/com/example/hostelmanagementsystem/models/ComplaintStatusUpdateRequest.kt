package com.example.hostelmanagementsystem.models

data class ComplaintStatusUpdateRequest(
    val adminId: String,
    val status: String,
    val adminNotes: String
)
