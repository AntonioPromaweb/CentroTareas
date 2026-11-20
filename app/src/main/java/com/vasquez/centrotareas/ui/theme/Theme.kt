package com.vasquez.centrotareas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CianPrimario,
    onPrimary = Color(0xFF03040C),
    secondary = VioletaSecundario,
    onSecondary = Color(0xFF03040C),
    background = FondoCosmico,
    onBackground = TextoClaroCosmico,
    surface = SuperficieCosmica,
    onSurface = TextoClaroCosmico,
    error = ErrorRojo,
    onError = ErrorFondo,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorRojo
)

@Composable
fun CentroTareasTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
