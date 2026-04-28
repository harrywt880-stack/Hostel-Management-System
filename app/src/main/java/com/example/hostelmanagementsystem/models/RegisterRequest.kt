package com.example.hostelmanagementsystem.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)
