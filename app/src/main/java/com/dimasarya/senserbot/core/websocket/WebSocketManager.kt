package com.dimasarya.senserbot.core.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager(
    okHttpClient: OkHttpClient,
    private val url: String = DEFAULT_URL
) {

    companion object {
        const val DEFAULT_URL = "ws://10.0.2.2:8000/ws"
        private val BACKOFF_DELAYS_MS = listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L)
        private const val MAX_BACKOFF_MS = 16_000L
    }

    // Derive client with ping interval to detect silently dead connections
    private val wsClient: OkHttpClient = okHttpClient.newBuilder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectMutex = Mutex()

    // DROP_OLDEST makes the drop policy explicit instead of silently blocking
    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var userDisconnected = false

    fun connect() {
        userDisconnected = false
        scope.launch { openSocket() }
    }

    fun send(json: String): Boolean = webSocket?.send(json) ?: false

    fun disconnect() {
        userDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    // Call from Application.onTerminate() to cancel the internal coroutine scope
    fun close() {
        disconnect()
        scope.cancel()
    }

    private suspend fun openSocket() = connectMutex.withLock {
        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder().url(url).build()
        webSocket = wsClient.newWebSocket(request, createListener())
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = ConnectionState.Connected
            reconnectJob?.cancel()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // tryEmit is non-blocking and thread-safe; no scope.launch needed
            _messages.tryEmit(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (!userDisconnected) {
                _connectionState.value = ConnectionState.Disconnected
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect() {
        if (userDisconnected) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var attempt = 0
            // Infinite retry — keeps trying at MAX_BACKOFF_MS after exhausting the list
            while (!userDisconnected) {
                val delayMs = BACKOFF_DELAYS_MS.getOrElse(attempt) { MAX_BACKOFF_MS }
                _connectionState.value = ConnectionState.Reconnecting
                delay(delayMs)
                if (userDisconnected) return@launch
                openSocket()
                delay(3_000L)
                if (_connectionState.value == ConnectionState.Connected) return@launch
                attempt++
            }
        }
    }
}
