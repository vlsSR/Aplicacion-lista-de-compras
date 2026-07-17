package com.example.aplicacionlistadecompras.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.aplicacionlistadecompras.data.model.Product
import com.example.aplicacionlistadecompras.ui.util.formatCreatedAt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listName: String,
    createdAt: Long,
    viewModel: ListDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (createdAt > 0L) {
                        Text(
                            text = formatCreatedAt(createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir producto")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.products.isEmpty() -> {
                    Text(
                        text = "La lista está vacía.\nToca + para añadir productos.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> {
                    val sorted = uiState.products.sortedBy { it.checked }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(sorted, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                onToggleChecked = { viewModel.toggleChecked(product) },
                                onQuantityChange = { newQty ->
                                    viewModel.updateQuantity(product, newQty)
                                },
                                onDelete = { viewModel.deleteProduct(product) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, quantity ->
                viewModel.addProduct(name, quantity)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onToggleChecked: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleChecked() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = product.checked,
            onCheckedChange = { onToggleChecked() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                checkmarkColor = MaterialTheme.colorScheme.onSecondary
            )
        )

        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyLarge.let {
                if (product.checked) it.copy(textDecoration = TextDecoration.LineThrough) else it
            },
            color = if (product.checked)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(enabled = false) {}
        ) {
            IconButton(
                onClick = { onQuantityChange(product.quantity - 1) },
                enabled = product.quantity > 1
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Menos",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = product.quantity.toString(),
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { onQuantityChange(product.quantity + 1) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Más",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Borrar producto",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Nuevo producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Producto (ej: Leche)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad:", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Menos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(quantity.toString(), color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { quantity++ }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Más",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, quantity) },
                enabled = name.isNotBlank()
            ) {
                Text("Añadir", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}