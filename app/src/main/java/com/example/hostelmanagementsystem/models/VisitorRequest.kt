package com.example.hostelmanagementsystem.models

data class VisitorRequest(
    val adminId: String,
    val userId: String,
    val visitorName: String,
    val visitorPhone: String,
    val relationToUser: String,
    val purpose: String,
    val visitDate: String
)
