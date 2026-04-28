package com.example.hostelmanagementsystem.models

data class RoomNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String,
    val room: Room?,
    val noticeId: String?
)
