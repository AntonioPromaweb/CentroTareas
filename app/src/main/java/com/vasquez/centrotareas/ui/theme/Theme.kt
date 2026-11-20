package com.vasquez.centrotareas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AzulPrimarioOscuro,
    onPrimary = FondoOscuro,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    error = Color(0xFFF2B8B5)
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = AzulSobrePrimario,
    primaryContainer = AzulContenedor,
    background = FondoClaro,
    surface = SuperficieClara,
    onBackground = TextoClaro,
    onSurface = TextoClaro,
    error = ErrorRojo,
    errorContainer = ErrorFondo
)

@Composable
fun CentroTareasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
