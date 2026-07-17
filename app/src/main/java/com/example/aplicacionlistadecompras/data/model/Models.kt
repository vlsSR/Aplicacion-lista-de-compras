package com.example.aplicacionlistadecompras.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: Long = 0L
)

@Serializable
data class Product(
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Int = 1,
    val checked: Boolean = false
)

// Body que se envía al crear una lista
@Serializable
data class CreateListRequest(val name: String)

// Body que se envía al crear un producto
@Serializable
data class CreateProductRequest(val name: String, val quantity: Int = 1)

// Body que se envía al actualizar un producto (todos los campos opcionales)
@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val quantity: Int? = null,
    val checked: Boolean? = null
)