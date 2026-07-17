package com.example.aplicacionlistadecompras.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.aplicacionlistadecompras.data.repository.ShoppingRepository
import com.example.aplicacionlistadecompras.ui.detail.ListDetailViewModel
import com.example.aplicacionlistadecompras.ui.lists.ListsViewModel

class ShoppingViewModelFactory(
    private val repository: ShoppingRepository,
    private val listId: String? = null // solo se usa para ListDetailViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(ListsViewModel::class.java) ->
                ListsViewModel(repository) as T

            modelClass.isAssignableFrom(ListDetailViewModel::class.java) ->
                ListDetailViewModel(repository, requireNotNull(listId)) as T

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}