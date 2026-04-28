package com.example.hostelmanagementsystem.models

data class Notice(
    val id: String,
    val title: String,
    val message: String,
    val audienceCount: Int,
    val createdAt: String,
    val createdBy: User?
)
