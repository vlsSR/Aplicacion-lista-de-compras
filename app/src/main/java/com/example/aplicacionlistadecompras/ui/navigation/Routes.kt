package com.example.aplicacionlistadecompras.ui.navigation

sealed class Routes(val route: String) {
    object Setup : Routes("setup")
    object Lists : Routes("lists")
    object ListDetail : Routes("list_detail/{listId}/{listName}/{createdAt}") {
        fun createRoute(listId: String, listName: String, createdAt: Long): String {
            val encodedName = java.net.URLEncoder.encode(listName, "UTF-8")
            return "list_detail/$listId/$encodedName/$createdAt"
        }
    }
}