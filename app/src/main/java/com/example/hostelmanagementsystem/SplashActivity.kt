package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.CurrentUserResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                validateSavedSession()
            } else {
                openRoleSelection()
            }
        }, 2500)
    }

    private fun validateSavedSession() {
        ApiClient.apiService.getCurrentUser()
            .enqueue(object : Callback<CurrentUserResponse> {
                override fun onResponse(
                    call: Call<CurrentUserResponse>,
                    response: Response<CurrentUserResponse>
                ) {
                    val user = response.body()?.user

                    if (response.isSuccessful && user != null) {
                        sessionManager.saveSession(
                            userId = user.id,
                            userName = user.name,
                            userEmail = user.email,
                            userRole = user.role,
                            token = sessionManager.getToken()
                        )
                        openDashboard(user.role)
                    } else {
                        sessionManager.clearSession()
                        openRoleSelection()
                    }
                }

                override fun onFailure(call: Call<CurrentUserResponse>, t: Throwable) {
                    sessionManager.clearSession()
                    openRoleSelection()
                }
            })
    }

    private fun openDashboard(role: String) {
        val nextScreen = if (role == "admin") {
            DashboardActivity::class.java
        } else {
            UserDashboardActivity::class.java
        }

        startActivity(Intent(this, nextScreen))
        finish()
    }

    private fun openRoleSelection() {
        startActivity(Intent(this, RoleSelectionActivity::class.java))
        finish()
    }
}
