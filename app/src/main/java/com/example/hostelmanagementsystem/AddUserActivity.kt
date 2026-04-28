package com.example.hostelmanagementsystem

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hostelmanagementsystem.models.LoginResponse
import com.example.hostelmanagementsystem.models.RegisterRequest
import com.example.hostelmanagementsystem.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddUserActivity : AppCompatActivity() {

    private var publicSignupMode: Boolean = false
    private lateinit var backText: TextView
    private lateinit var titleText: TextView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var roleInput: AutoCompleteTextView
    private lateinit var addUserBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        publicSignupMode = intent.getBooleanExtra(EXTRA_PUBLIC_SIGNUP, false)
        backText = findViewById(R.id.backText)
        titleText = findViewById(R.id.titleText)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        roleInput = findViewById(R.id.roleInput)
        addUserBtn = findViewById(R.id.addUserBtn)

        roleInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listOf("admin", "warden", "student")
            )
        )
        roleInput.setText("student", false)

        if (publicSignupMode) {
            titleText.text = "Create Student Account"
            roleInput.visibility = View.GONE
            addUserBtn.text = "Create Account"
        }

        backText.setOnClickListener { finish() }
        addUserBtn.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val role = if (publicSignupMode) "student" else roleInput.text.toString().trim().lowercase()

        if (name.isEmpty()) {
            nameInput.error = "Name required"
            return
        }

        if (email.isEmpty()) {
            emailInput.error = "Email required"
            return
        }

        if (password.length < 6) {
            passwordInput.error = "Minimum 6 characters"
            return
        }

        if (role !in listOf("admin", "warden", "student")) {
            roleInput.error = "Choose admin, warden or student"
            return
        }

        addUserBtn.isEnabled = false
        addUserBtn.text = "Creating..."

        ApiClient.apiService.registerUser(RegisterRequest(name, email, password, role))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    addUserBtn.isEnabled = true
                    addUserBtn.text = if (publicSignupMode) "Create Account" else "Create User"

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddUserActivity,
                            if (publicSignupMode) "Account created successfully" else "User created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddUserActivity,
                            if (publicSignupMode) "Failed to create account" else "Failed to create user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    addUserBtn.isEnabled = true
                    addUserBtn.text = if (publicSignupMode) "Create Account" else "Create User"

                    Toast.makeText(
                        this@AddUserActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    companion object {
        const val EXTRA_PUBLIC_SIGNUP = "public_signup"
    }
}
