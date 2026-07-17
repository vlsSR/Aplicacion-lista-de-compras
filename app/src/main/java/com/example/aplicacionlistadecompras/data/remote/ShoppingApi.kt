package com.example.aplicacionlistadecompras.data.remote

import com.example.aplicacionlistadecompras.data.model.*
import retrofit2.http.*

interface ShoppingApi {

    @GET("api/lists")
    suspend fun getLists(): List<ShoppingList>

    @POST("api/lists")
    suspend fun createList(@Body request: CreateListRequest): ShoppingList

    @DELETE("api/lists/{listId}")
    suspend fun deleteList(@Path("listId") listId: String)

    @GET("api/lists/{listId}/products")
    suspend fun getProducts(@Path("listId") listId: String): List<Product>

    @POST("api/lists/{listId}/products")
    suspend fun createProduct(
        @Path("listId") listId: String,
        @Body request: CreateProductRequest
    ): Product

    @PUT("api/products/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: String,
        @Body request: UpdateProductRequest
    ): Product

    @DELETE("api/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: String)
}