package com.example.hostelmanagementsystem.models

data class LoginResponse(
    val message: String,
    val user: User,
    val token: String
)