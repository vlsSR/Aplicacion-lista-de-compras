package com.example.aplicacionlistadecompras.data.remote

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class SseClient(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Escucha UNA conexión SSE. Si se cierra o falla, termina el Flow
     * (no reintenta aquí dentro) — la reconexión con espera progresiva
     * la gestiona quien recolecte este Flow (ShoppingRepository con retryWhen).
     */
    fun listen(): Flow<SseEnvelope> = callbackFlow {
        val request = Request.Builder()
            .url("${baseUrl}api/events")
            .build()

        val eventSource = EventSources.createFactory(okHttpClient)
            .newEventSource(request, object : EventSourceListener() {

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    runCatching {
                        json.decodeFromString<SseEnvelope>(data)
                    }.onSuccess { envelope ->
                        trySend(envelope)
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    close() // termina este Flow -> retryWhen en el repository reconectará
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: okhttp3.Response?
                ) {
                    close(t ?: Exception("Conexión SSE fallida"))
                }
            })

        awaitClose { eventSource.cancel() }
    }
}