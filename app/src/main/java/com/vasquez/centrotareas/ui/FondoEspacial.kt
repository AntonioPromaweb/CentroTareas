package com.vasquez.centrotareas.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

private data class Estrella(
    val x: Float,
    val y0: Float,
    val radioDp: Float,
    val capa: Int,
    val velocidadCapa: Float,
    val alfaBase: Float,
    val faseParpadeo: Float
)

@Composable
fun FondoEspacial(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "animacionEspacio")

    val tProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progresoMovimiento"
    )

    val parpadeoProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progresoParpadeo"
    )

    val estrellas = remember {
        val random = Random(12345)
        List(140) {
            val capa = when (random.nextFloat()) {
                in 0.0f..0.5f -> 0
                in 0.5f..0.85f -> 1
                else -> 2
            }
            val (radioMin, radioMax, vel, baseAlpha) = when (capa) {
                0 -> Quadruple(0.6f, 1.2f, 1f, 0.45f)
                1 -> Quadruple(1.2f, 2.0f, 2f, 0.70f)
                else -> Quadruple(2.0f, 3.2f, 3f, 0.90f)
            }
            val radioDp = radioMin + random.nextFloat() * (radioMax - radioMin)
            val alfaBase = (baseAlpha + (random.nextFloat() * 0.2f - 0.1f)).coerceIn(0.2f, 1.0f)

            Estrella(
                x = random.nextFloat(),
                y0 = random.nextFloat(),
                radioDp = radioDp,
                capa = capa,
                velocidadCapa = vel,
                alfaBase = alfaBase,
                faseParpadeo = random.nextFloat() * (2.0 * Math.PI).toFloat()
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03040C),
                        Color(0xFF0D1236)
                    )
                )
            )

            val t = tProgress.value
            val p = parpadeoProgress.value
            val w = size.width
            val h = size.height

            for (e in estrellas) {
                val yNorm = (e.y0 + t * e.velocidadCapa) % 1.0f
                val xPx = e.x * w
                val yPx = yNorm * h

                val twinkle = 0.25f * sin(p + e.faseParpadeo)
                val alphaFinal = (e.alfaBase + twinkle).coerceIn(0.15f, 1.0f)

                drawCircle(
                    color = Color.White.copy(alpha = alphaFinal),
                    radius = e.radioDp.dp.toPx(),
                    center = Offset(xPx, yPx)
                )
            }
        }

        content()
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
