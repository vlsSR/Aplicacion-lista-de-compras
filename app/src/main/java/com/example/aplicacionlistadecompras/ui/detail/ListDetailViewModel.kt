package com.example.aplicacionlistadecompras.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacionlistadecompras.data.model.Product
import com.example.aplicacionlistadecompras.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListDetailUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ListDetailViewModel(
    private val repository: ShoppingRepository,
    private val listId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailUiState(isLoading = true))
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.productsByList.collect { map ->
                android.util.Log.d("DEBUG_APP", "collect productsByList: recibido, contiene listId=${map.containsKey(listId)}, total claves=${map.keys}")
                val products = map[listId].orEmpty()
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
                android.util.Log.d("DEBUG_APP", "collect productsByList: isLoading puesto a false para $listId")
            }
        }
        loadInitial()
    }

    private fun loadInitial() {
        android.util.Log.d("DEBUG_APP", "loadInitial: lanzando para listId=$listId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            android.util.Log.d("DEBUG_APP", "loadInitial: antes de runCatching")
            runCatching { repository.refreshProducts(listId) }
                .onSuccess {
                    android.util.Log.d("DEBUG_APP", "loadInitial: éxito")
                }
                .onFailure { e ->
                    android.util.Log.e("DEBUG_APP", "loadInitial: FALLO", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con el servidor"
                    )
                }
        }
    }

    fun addProduct(name: String, quantity: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.createProduct(listId, name.trim(), quantity) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo añadir el producto")
                }
        }
    }

    fun toggleChecked(product: Product) {
        viewModelScope.launch {
            runCatching { repository.toggleChecked(product) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo actualizar el producto")
                }
        }
    }

    fun updateQuantity(product: Product, quantity: Int) {
        if (quantity < 1) return
        viewModelScope.launch {
            runCatching { repository.updateQuantity(product, quantity) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo actualizar la cantidad")
                }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(product) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo borrar el producto")
                }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}