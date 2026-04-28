package com.example.hostelmanagementsystem.network

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(userId: String, userName: String, userEmail: String, userRole: String, token: String) {
        preferences.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_USER_ROLE, userRole)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !preferences.getString(KEY_TOKEN, null).isNullOrBlank()
    }

    fun getUserName(): String {
        return preferences.getString(KEY_USER_NAME, "Admin") ?: "Admin"
    }

    fun getUserId(): String {
        return preferences.getString(KEY_USER_ID, "") ?: ""
    }

    fun getUserEmail(): String {
        return preferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getUserRole(): String {
        return preferences.getString(KEY_USER_ROLE, "admin") ?: "admin"
    }

    fun getToken(): String {
        return preferences.getString(KEY_TOKEN, "") ?: ""
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "hostel_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TOKEN = "token"
    }
}
