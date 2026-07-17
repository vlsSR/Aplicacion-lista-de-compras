package com.example.aplicacionlistadecompras.data.repository

import com.example.aplicacionlistadecompras.data.model.*
import com.example.aplicacionlistadecompras.data.remote.NetworkModule
import com.example.aplicacionlistadecompras.data.remote.SseClient
import com.example.aplicacionlistadecompras.data.remote.SseEnvelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

class ShoppingRepository(
    private val network: NetworkModule
) {
    private val sseClient = SseClient(network.sseOkHttpClient, network.baseUrl)  // ← cambio aquí


    // Scope propio para la escucha SSE en segundo plano, vive mientras viva el repository
    private val repoScope = CoroutineScope(SupervisorJob())

    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists.asStateFlow()

    // Productos agrupados por listId, para que cada pantalla de detalle coja los suyos
    private val _productsByList = MutableStateFlow<Map<String, List<Product>>>(emptyMap())
    val productsByList: StateFlow<Map<String, List<Product>>> = _productsByList.asStateFlow()

    init {
        startListeningToEvents()
    }

    // --- Carga inicial (REST) ---

    suspend fun refreshLists() {
        _lists.value = network.api.getLists()
    }

    suspend fun refreshProducts(listId: String) {
        android.util.Log.d("DEBUG_APP", "refreshProducts: empezando para listId=$listId")
        val products = network.api.getProducts(listId)
        android.util.Log.d("DEBUG_APP", "refreshProducts: recibidos ${products.size} productos")
        _productsByList.value = _productsByList.value.toMutableMap().apply {
            put(listId, products)
        }
        android.util.Log.d("DEBUG_APP", "refreshProducts: estado actualizado")
    }

    // --- Acciones (REST); el propio backend nos devolverá el cambio también por SSE,
    // pero actualizamos aquí también de inmediato para que la UI responda al instante
    // sin esperar el "eco" del evento ---

    suspend fun createList(name: String) {
        val created = network.api.createList(CreateListRequest(name))
        _lists.value = _lists.value + created
    }

    suspend fun deleteList(listId: String) {
        network.api.deleteList(listId)
        _lists.value = _lists.value.filterNot { it.id == listId }
    }

    suspend fun createProduct(listId: String, name: String, quantity: Int) {
        val created = network.api.createProduct(listId, CreateProductRequest(name, quantity))
        upsertProduct(listId, created)
    }

    suspend fun toggleChecked(product: Product) {
        val updated = network.api.updateProduct(
            product.id,
            UpdateProductRequest(checked = !product.checked)
        )
        upsertProduct(product.listId, updated)
    }

    suspend fun updateQuantity(product: Product, quantity: Int) {
        val updated = network.api.updateProduct(
            product.id,
            UpdateProductRequest(quantity = quantity)
        )
        upsertProduct(product.listId, updated)
    }

    suspend fun deleteProduct(product: Product) {
        network.api.deleteProduct(product.id)
        _productsByList.value = _productsByList.value.toMutableMap().apply {
            val current = get(product.listId).orEmpty()
            put(product.listId, current.filterNot { it.id == product.id })
        }
    }

    // --- Escucha SSE: aquí es donde otros dispositivos "empujan" cambios ---

    private fun startListeningToEvents() {
        repoScope.launch {
            sseClient.listen()
                .retryWhen { _, attempt ->
                    // si la conexión falla, espera un poco más cada vez (máx 10s) y reintenta
                    delay(minOf(2000L * (attempt + 1), 10_000L))
                    true
                }
                .catch { /* evita que un error tumbe la corrutina; retryWhen ya reconecta */ }
                .collect { envelope -> handleEvent(envelope) }
        }
    }

    private fun handleEvent(envelope: SseEnvelope) {
        when (envelope.type) {
            "LIST_CREATED" -> envelope.listPayload?.let { newList ->
                if (_lists.value.none { it.id == newList.id }) {
                    _lists.value = _lists.value + newList
                }
            }
            "LIST_DELETED" -> envelope.deletedId?.let { id ->
                _lists.value = _lists.value.filterNot { it.id == id }
            }
            "PRODUCT_CREATED", "PRODUCT_UPDATED" -> envelope.productPayload?.let { product ->
                upsertProduct(product.listId, product)
            }
            "PRODUCT_DELETED" -> envelope.deletedId?.let { id ->
                _productsByList.value = _productsByList.value.mapValues { (_, products) ->
                    products.filterNot { it.id == id }
                }
            }
        }
    }

    private fun upsertProduct(listId: String, product: Product) {
        _productsByList.value = _productsByList.value.toMutableMap().apply {
            val current = get(listId).orEmpty()
            val exists = current.any { it.id == product.id }
            put(
                listId,
                if (exists) current.map { if (it.id == product.id) product else it }
                else current + product
            )
        }
    }
}