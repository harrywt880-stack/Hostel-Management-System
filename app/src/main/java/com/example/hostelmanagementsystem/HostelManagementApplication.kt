package com.example.hostelmanagementsystem

import android.app.Application
import com.example.hostelmanagementsystem.network.ApiClient

class HostelManagementApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
