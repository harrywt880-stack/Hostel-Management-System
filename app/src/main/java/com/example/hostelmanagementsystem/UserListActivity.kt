package com.example.hostelmanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hostelmanagementsystem.adapters.UserAdapter
import com.example.hostelmanagementsystem.models.User
import com.example.hostelmanagementsystem.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserListActivity : AppCompatActivity() {

    private lateinit var backText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var loadingText: TextView
    private lateinit var addUserBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        backText = findViewById(R.id.backText)
        recyclerView = findViewById(R.id.usersRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        loadingText = findViewById(R.id.loadingText)
        addUserBtn = findViewById(R.id.addUserBtn)

        recyclerView.layoutManager = LinearLayoutManager(this)

        backText.setOnClickListener {
            finish()
        }

        addUserBtn.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUsers()
    }

    private fun fetchUsers() {
        loadingText.text = "Loading users..."

        ApiClient.apiService.getUsers()
            .enqueue(object : Callback<List<User>> {

                override fun onResponse(
                    call: Call<List<User>>,
                    response: Response<List<User>>
                ) {
                    loadingText.text = ""

                    if (response.isSuccessful && response.body() != null) {
                        val users = response.body()!!

                        if (users.isEmpty()) {
                            emptyText.text = "No users found"
                        } else {
                            emptyText.text = ""
                            recyclerView.adapter = UserAdapter(users)
                        }

                    } else {
                        Toast.makeText(
                            this@UserListActivity,
                            "Failed to load users",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    loadingText.text = ""

                    Toast.makeText(
                        this@UserListActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
