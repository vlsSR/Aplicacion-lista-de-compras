package com.example.aplicacionlistadecompras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.aplicacionlistadecompras.ui.theme.BackgroundDark
import com.example.aplicacionlistadecompras.ui.theme.PastelCanela
import com.example.aplicacionlistadecompras.ui.theme.PastelCeleste
import com.example.aplicacionlistadecompras.ui.theme.PastelRojo
import com.example.aplicacionlistadecompras.ui.theme.PastelVerde
import com.example.aplicacionlistadecompras.ui.theme.SurfaceDark
import com.example.aplicacionlistadecompras.ui.theme.SurfaceVariantDark
import com.example.aplicacionlistadecompras.ui.theme.TextPrimary
import com.example.aplicacionlistadecompras.ui.theme.TextSecondary

private val AppColorScheme = darkColorScheme(
    primary = PastelCanela,
    onPrimary = Color(0xFF2A1D10),
    secondary = PastelCeleste,
    onSecondary = Color(0xFF10242A),
    tertiary = PastelVerde,
    error = PastelRojo,
    onError = Color(0xFF2A1010),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary
)

@Composable
fun ShoppingListAppTheme(content: @Composable () -> Unit) {
    // Forzamos siempre el esquema oscuro, ignorando el tema del sistema
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}