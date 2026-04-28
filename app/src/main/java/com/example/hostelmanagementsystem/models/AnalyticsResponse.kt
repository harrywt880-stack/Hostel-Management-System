package com.example.hostelmanagementsystem.models

data class AnalyticsResponse(
    val generatedAt: String,
    val snapshot: AnalyticsSnapshot,
    val daily: AnalyticsPeriod,
    val weekly: AnalyticsPeriod,
    val monthly: AnalyticsPeriod
)
