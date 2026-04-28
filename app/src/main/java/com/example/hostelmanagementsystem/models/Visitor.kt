package com.example.hostelmanagementsystem.models

data class Visitor(
    val id: String,
    val visitorName: String,
    val visitorPhone: String,
    val relationToUser: String,
    val purpose: String,
    val visitDate: String,
    val createdAt: String,
    val user: User?,
    val createdBy: User?
)
