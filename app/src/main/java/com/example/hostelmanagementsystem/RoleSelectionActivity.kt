package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.ui.UiEffects

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var adminLoginBtn: Button
    private lateinit var userLoginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)
        UiEffects.animateScreenIn(this)

        adminLoginBtn = findViewById(R.id.adminLoginBtn)
        userLoginBtn = findViewById(R.id.userLoginBtn)

        adminLoginBtn.setOnClickListener {
            UiEffects.pulse(adminLoginBtn) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        userLoginBtn.setOnClickListener {
            UiEffects.pulse(userLoginBtn) {
                startActivity(Intent(this, UserLoginActivity::class.java))
            }
        }
    }
}
