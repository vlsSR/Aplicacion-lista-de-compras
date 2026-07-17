package com.example.aplicacionlistadecompras.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacionlistadecompras.data.model.ShoppingList
import com.example.aplicacionlistadecompras.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ListsViewModel(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsUiState(isLoading = true))
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    init {
        // Escucha continua del repositorio (REST inicial + eventos SSE ya combinados ahí)
        viewModelScope.launch {
            repository.lists.collect { lists ->
                _uiState.value = _uiState.value.copy(lists = lists, isLoading = false)
            }
        }
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.refreshLists() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con el servidor"
                    )
                }
        }
    }

    fun createList(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.createList(name.trim()) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo crear la lista")
                }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteList(listId) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudo borrar la lista")
                }
        }
    }

    fun retry() = loadInitial()

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}