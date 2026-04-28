package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.LoginRequest
import com.example.hostelmanagementsystem.models.LoginResponse
import com.example.hostelmanagementsystem.network.ApiClient
import com.example.hostelmanagementsystem.network.SessionManager
import com.example.hostelmanagementsystem.ui.UiEffects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserLoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var loginContainer: LinearLayout
    private lateinit var createAccountText: TextView
    private lateinit var backText: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginBtn = findViewById(R.id.loginBtn)
        loginContainer = findViewById(R.id.loginContainer)
        createAccountText = findViewById(R.id.createAccountText)
        backText = findViewById(R.id.backText)
        sessionManager = SessionManager(this)

        val fadeSlide = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        loginContainer.startAnimation(fadeSlide)
        UiEffects.animateScreenIn(this)

        backText.setOnClickListener { finish() }

        loginBtn.setOnClickListener {
            UiEffects.pulse(loginBtn)
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error = "Email required"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password required"
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        createAccountText.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra(AddUserActivity.EXTRA_PUBLIC_SIGNUP, true)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        loginBtn.isEnabled = false
        loginBtn.text = "Logging in..."

        ApiClient.apiService.loginUser(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    loginBtn.isEnabled = true
                    loginBtn.text = "User Login"

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        if (data.user.role == "admin") {
                            Toast.makeText(
                                this@UserLoginActivity,
                                "Please use admin login for admin accounts",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        sessionManager.saveSession(
                            userId = data.user.id,
                            userName = data.user.name,
                            userEmail = data.user.email,
                            userRole = data.user.role,
                            token = data.token
                        )

                        startActivity(Intent(this@UserLoginActivity, UserDashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@UserLoginActivity,
                            "Invalid email or password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    loginBtn.isEnabled = true
                    loginBtn.text = "User Login"

                    Toast.makeText(
                        this@UserLoginActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
