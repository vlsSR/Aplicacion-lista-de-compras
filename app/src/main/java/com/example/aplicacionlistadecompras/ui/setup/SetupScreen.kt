package com.example.aplicacionlistadecompras.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(onUrlSaved: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun isValidUrl(input: String): Boolean {
        val trimmed = input.trim()
        return (trimmed.startsWith("http://") || trimmed.startsWith("https://")) &&
                trimmed.length > "https://".length
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configura el servidor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Introduce el enlace del backend familiar")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                errorText = null
            },
            label = { Text("https://mi-backend.onrender.com") },
            singleLine = true,
            isError = errorText != null,
            supportingText = {
                if (errorText != null) Text(errorText!!)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (isValidUrl(url)) {
                    onUrlSaved(url.trim())
                } else {
                    errorText = "Introduce una URL válida (debe empezar por http:// o https://)"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y continuar")
        }
    }
}