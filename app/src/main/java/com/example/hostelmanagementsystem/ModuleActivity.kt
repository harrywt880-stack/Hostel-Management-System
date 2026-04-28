package com.example.hostelmanagementsystem

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ModuleActivity : AppCompatActivity() {

    private lateinit var moduleTitle: TextView
    private lateinit var moduleSubtitle: TextView
    private lateinit var backText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)

        moduleTitle = findViewById(R.id.moduleTitle)
        moduleSubtitle = findViewById(R.id.moduleSubtitle)
        backText = findViewById(R.id.backText)

        val title = intent.getStringExtra("MODULE_TITLE") ?: "Module"

        moduleTitle.text = title
        moduleSubtitle.text = "Manage $title from here"

        backText.setOnClickListener {
            finish()
        }
    }
}