package com.example.aplicacionlistadecompras.data.remote

import com.example.aplicacionlistadecompras.data.model.Product
import com.example.aplicacionlistadecompras.data.model.ShoppingList
import kotlinx.serialization.Serializable

@Serializable
data class SseEnvelope(
    val type: String, // "LIST_CREATED", "LIST_DELETED", "PRODUCT_CREATED", "PRODUCT_UPDATED", "PRODUCT_DELETED"
    val listPayload: ShoppingList? = null,
    val productPayload: Product? = null,
    val deletedId: String? = null
)