package com.example.aplicacionlistadecompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicacionlistadecompras.data.local.SettingsRepository
import com.example.aplicacionlistadecompras.data.remote.NetworkModule
import com.example.aplicacionlistadecompras.data.repository.ShoppingRepository
import com.example.aplicacionlistadecompras.ui.ShoppingViewModelFactory
import com.example.aplicacionlistadecompras.ui.detail.ListDetailScreen
import com.example.aplicacionlistadecompras.ui.detail.ListDetailViewModel
import com.example.aplicacionlistadecompras.ui.lists.ListsScreen
import com.example.aplicacionlistadecompras.ui.lists.ListsViewModel
import com.example.aplicacionlistadecompras.ui.navigation.Routes
import com.example.aplicacionlistadecompras.ui.setup.SetupScreen
import com.example.aplicacionlistadecompras.ui.theme.ShoppingListAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            ShoppingListAppTheme {
                AppRoot(settingsRepository = settingsRepository)
            }
        }
    }
}

@Composable
fun AppRoot(settingsRepository: SettingsRepository) {
    val backendUrl by settingsRepository.backendUrl.collectAsState(initial = "LOADING")
    val scope = rememberCoroutineScope()
    var setupError by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        when (val url = backendUrl) {
            "LOADING" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            null -> {
                SetupScreen(onUrlSaved = { newUrl ->
                    scope.launch { settingsRepository.saveBackendUrl(newUrl) }
                })
            }
            else -> {
                val repository = remember(url) {
                    runCatching {
                        val network = NetworkModule(url)
                        ShoppingRepository(network)
                    }.getOrNull()
                }

                if (repository == null) {
                    // La URL guardada no es válida (raro, pero por si acaso) -> pedimos otra
                    LaunchedEffect(Unit) {
                        settingsRepository.saveBackendUrl("") // fuerza volver a pedirla
                    }
                    SetupScreen(onUrlSaved = { newUrl ->
                        scope.launch { settingsRepository.saveBackendUrl(newUrl) }
                    })
                } else {
                    MainNavHost(repository = repository, settingsRepository = settingsRepository)
                }
            }
        }
    }
}

@Composable
fun MainNavHost(
    repository: ShoppingRepository,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Routes.Lists.route) {

        composable(Routes.Lists.route) {
            val viewModel: ListsViewModel = viewModel(
                factory = ShoppingViewModelFactory(repository)
            )
            ListsScreen(
                viewModel = viewModel,
                onListClick = { list ->
                    navController.navigate(
                        Routes.ListDetail.createRoute(list.id, list.name, list.createdAt)
                    )
                },
                onSettingsClick = {
                    scope.launch { settingsRepository.clearBackendUrl() }
                }
            )
        }

        composable(
            route = Routes.ListDetail.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.StringType },
                navArgument("listName") { type = NavType.StringType },
                navArgument("createdAt") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val encodedName = backStackEntry.arguments?.getString("listName") ?: ""
            val listName = java.net.URLDecoder.decode(encodedName, "UTF-8")
            val createdAt = backStackEntry.arguments?.getLong("createdAt") ?: 0L

            val viewModel: ListDetailViewModel = viewModel(
                factory = ShoppingViewModelFactory(repository, listId = listId)
            )
            ListDetailScreen(
                listName = listName,
                createdAt = createdAt,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}