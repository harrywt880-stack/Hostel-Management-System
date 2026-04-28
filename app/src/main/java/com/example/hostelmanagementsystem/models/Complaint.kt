package com.example.hostelmanagementsystem.models

data class Complaint(
    val id: String,
    val complaintId: String,
    val subject: String,
    val description: String,
    val status: String,
    val adminNotes: String,
    val resolvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val user: User?
)
