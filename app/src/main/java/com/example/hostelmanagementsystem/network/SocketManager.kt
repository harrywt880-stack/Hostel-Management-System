package com.example.hostelmanagementsystem.network

import com.example.hostelmanagementsystem.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {

    private var socket: Socket? = null

    fun getSocket(): Socket {
        if (socket == null) {
            socket = IO.socket(BuildConfig.SOCKET_URL)
        }

        return socket!!
    }
}
