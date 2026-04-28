package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.network.SessionManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(this)
            val nextScreen = if (sessionManager.isLoggedIn()) {
                if (sessionManager.getUserRole() == "admin") {
                    DashboardActivity::class.java
                } else {
                    UserDashboardActivity::class.java
                }
            } else {
                RoleSelectionActivity::class.java
            }

            startActivity(Intent(this, nextScreen))
            finish()
        }, 2500)
    }
}
