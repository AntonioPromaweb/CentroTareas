package com.vasquez.centrotareas.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

enum class EstadoAvatar {
    Reposo, Trabajando, Exito, Error
}

@Composable
fun AvatarCohete(
    estado: EstadoAvatar,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "animCohete")

    val floatAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatAnim"
    )

    val vibAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vibAnim"
    )

    val llamaAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "llamaAnim"
    )

    val jumpAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jumpAnim"
    )

    Canvas(modifier = modifier.size(44.dp)) {
        val fVal = floatAnim.value
        val vVal = vibAnim.value
        val lVal = llamaAnim.value
        val jVal = jumpAnim.value

        val w = size.width
        val h = size.height

        var offsetX = 0f
        var offsetY = 0f
        var rotationDeg = 0f

        if (estado == EstadoAvatar.Reposo) {
            offsetY = (fVal - 0.5f) * 6.dp.toPx()
        } else if (estado == EstadoAvatar.Trabajando) {
            offsetX = (vVal - 0.5f) * 4.dp.toPx()
            offsetY = (vVal - 0.5f) * 2.dp.toPx() - 2.dp.toPx()
        } else if (estado == EstadoAvatar.Exito) {
            offsetY = -jVal * 7.dp.toPx()
        } else if (estado == EstadoAvatar.Error) {
            rotationDeg = -15f
            offsetY = (fVal - 0.5f) * 3.dp.toPx()
        }

        withTransform({
            rotate(rotationDeg, pivot = Offset(w / 2f, h / 2f))
        }) {
            val cx = w / 2f + offsetX
            val cy = h / 2f + offsetY

            val bodyW = w * 0.36f
            val bodyH = h * 0.44f

            // 1. Llama (si está trabajando)
            if (estado == EstadoAvatar.Trabajando) {
                val flameH = 6.dp.toPx() + lVal * 6.dp.toPx()
                val flameW = bodyW * 0.7f
                val flameTopY = cy + bodyH / 2f

                val flamePath = Path().apply {
                    moveTo(cx - flameW / 2f, flameTopY)
                    lineTo(cx + flameW / 2f, flameTopY)
                    lineTo(cx, flameTopY + flameH)
                    close()
                }
                drawPath(flamePath, color = Color(0xFFFF7043))

                val innerFlamePath = Path().apply {
                    moveTo(cx - flameW / 4f, flameTopY)
                    lineTo(cx + flameW / 4f, flameTopY)
                    lineTo(cx, flameTopY + flameH * 0.6f)
                    close()
                }
                drawPath(innerFlamePath, color = Color(0xFFFFD54F))
            }

            // 2. Aletas violetas
            val finW = 6.dp.toPx()
            val finBotY = cy + bodyH / 2f
            val finTopY = cy + bodyH * 0.1f

            val leftFin = Path().apply {
                moveTo(cx - bodyW / 2f, finTopY)
                lineTo(cx - bodyW / 2f - finW, finBotY)
                lineTo(cx - bodyW / 2f, finBotY)
                close()
            }
            drawPath(leftFin, color = Color(0xFF9B7BFF))

            val rightFin = Path().apply {
                moveTo(cx + bodyW / 2f, finTopY)
                lineTo(cx + bodyW / 2f + finW, finBotY)
                lineTo(cx + bodyW / 2f, finBotY)
                close()
            }
            drawPath(rightFin, color = Color(0xFF9B7BFF))

            // 3. Cuerpo principal blanco-gris
            val bodyTopY = cy - bodyH / 2f
            val bodyBotY = cy + bodyH / 2f
            val noseH = 8.dp.toPx()

            val bodyPath = Path().apply {
                moveTo(cx, bodyTopY - noseH)
                lineTo(cx + bodyW / 2f, bodyTopY)
                lineTo(cx + bodyW / 2f, bodyBotY)
                lineTo(cx - bodyW / 2f, bodyBotY)
                lineTo(cx - bodyW / 2f, bodyTopY)
                close()
            }
            drawPath(bodyPath, color = Color(0xFFE2E8F0))

            // Color de ventana según estado
            val colorVentana = if (estado == EstadoAvatar.Exito) {
                Color(0xFF7CF0A5)
            } else if (estado == EstadoAvatar.Error) {
                Color(0xFFFF8A8A)
            } else {
                Color(0xFF4FD8FF)
            }

            val radiusVentana = bodyW * 0.28f
            val centerVentana = Offset(cx, cy - 2.dp.toPx())

            if (estado == EstadoAvatar.Exito || estado == EstadoAvatar.Error) {
                drawCircle(
                    color = colorVentana.copy(alpha = 0.35f),
                    radius = radiusVentana + 3.dp.toPx(),
                    center = centerVentana
                )
            }

            drawCircle(
                color = colorVentana,
                radius = radiusVentana,
                center = centerVentana
            )
            drawCircle(
                color = Color(0xFF37474F),
                radius = radiusVentana,
                center = centerVentana,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
fun AvatarPlaneta(
    estado: EstadoAvatar,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "animPlaneta")

    val rotAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotAnim"
    )

    val pulseAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Canvas(modifier = modifier.size(44.dp)) {
        val rVal = rotAnim.value
        val pVal = pulseAnim.value

        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.28f

        val multVel = if (estado == EstadoAvatar.Trabajando) 4f else 1f
        val angleDeg = (rVal * multVel * 360f) % 360f

        val colorAnillo = if (estado == EstadoAvatar.Exito) {
            Color(0xFF7CF0A5)
        } else if (estado == EstadoAvatar.Error) {
            Color(0xFFFF8A8A)
        } else {
            Color(0xFF4FD8FF)
        }

        val ringW = w * 0.82f
        val ringH = h * 0.26f
        val ringRect = Size(ringW, ringH)
        val ringTopLeft = Offset(cx - ringW / 2f, cy - ringH / 2f)

        // 1. Parte trasera del anillo (de 180° a 360°)
        withTransform({
            rotate(-20f, pivot = Offset(cx, cy))
        }) {
            if (estado == EstadoAvatar.Exito) {
                drawArc(
                    color = Color(0xFF7CF0A5).copy(alpha = 0.4f + pVal * 0.4f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringRect,
                    style = Stroke(width = 5.dp.toPx())
                )
            }
            drawArc(
                color = colorAnillo,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringRect,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 2. Esfera del planeta violeta
        drawCircle(
            color = Color(0xFF8E24AA),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Cráteres dentro del planeta
        withTransform({
            rotate(angleDeg, pivot = Offset(cx, cy))
        }) {
            drawCircle(
                color = Color(0xFF5E35B1),
                radius = radius * 0.25f,
                center = Offset(cx - radius * 0.35f, cy - radius * 0.3f)
            )
            drawCircle(
                color = Color(0xFF5E35B1),
                radius = radius * 0.18f,
                center = Offset(cx + radius * 0.35f, cy + radius * 0.35f)
            )
        }

        // 3. Parte delantera del anillo (de 0° a 180°)
        withTransform({
            rotate(-20f, pivot = Offset(cx, cy))
        }) {
            if (estado == EstadoAvatar.Exito) {
                drawArc(
                    color = Color(0xFF7CF0A5).copy(alpha = 0.4f + pVal * 0.4f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringRect,
                    style = Stroke(width = 5.dp.toPx())
                )
            }
            drawArc(
                color = colorAnillo,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringRect,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun AvatarSatelite(
    estado: EstadoAvatar,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "animSatelite")

    val floatAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatAnim"
    )

    val pulseAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    val waveAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAnim"
    )

    val shakeAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeAnim"
    )

    Canvas(modifier = modifier.size(44.dp)) {
        val fVal = floatAnim.value
        val pVal = pulseAnim.value
        val wVal = waveAnim.value
        val sVal = shakeAnim.value

        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        var offsetY = 0f
        var offsetX = 0f
        var scale = 1.0f

        if (estado == EstadoAvatar.Reposo) {
            offsetY = (fVal - 0.5f) * 6.dp.toPx()
        } else if (estado == EstadoAvatar.Trabajando) {
            scale = 0.94f + pVal * 0.12f
        } else if (estado == EstadoAvatar.Error) {
            offsetX = (sVal - 0.5f) * 6.dp.toPx()
        }

        withTransform({
            scale(scale, scale, pivot = Offset(cx, cy))
        }) {
            val centerSatX = cx + offsetX
            val centerSatY = cy + offsetY

            val bodySize = 12.dp.toPx()
            val antennaH = 9.dp.toPx()
            val antennaTipY = centerSatY - bodySize / 2f - antennaH

            // 1. Ondas de señal de la antena
            if (estado == EstadoAvatar.Trabajando) {
                val maxR = 12.dp.toPx()
                for (i in 0..1) {
                    val progress = (wVal + i * 0.5f) % 1.0f
                    val r = progress * maxR
                    val alpha = (1.0f - progress).coerceIn(0f, 1f)
                    drawArc(
                        color = Color(0xFF4FD8FF).copy(alpha = alpha),
                        startAngle = 210f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(centerSatX - r, antennaTipY - r),
                        size = Size(r * 2f, r * 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else if (estado == EstadoAvatar.Exito) {
                listOf(6.dp.toPx(), 11.dp.toPx()).forEach { r ->
                    drawArc(
                        color = Color(0xFF7CF0A5),
                        startAngle = 210f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(centerSatX - r, antennaTipY - r),
                        size = Size(r * 2f, r * 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else if (estado == EstadoAvatar.Error) {
                val r = 8.dp.toPx()
                drawArc(
                    color = Color(0xFFFF8A8A),
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(centerSatX - r, antennaTipY - r),
                    size = Size(r * 2f, r * 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 2. Antena (vara y bola)
            drawLine(
                color = Color(0xFF78909C),
                start = Offset(centerSatX, centerSatY - bodySize / 2f),
                end = Offset(centerSatX, antennaTipY),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = if (estado == EstadoAvatar.Error) Color(0xFFFF8A8A) else Color(0xFFB0BEC5),
                radius = 2.dp.toPx(),
                center = Offset(centerSatX, antennaTipY)
            )

            // 3. Paneles solares laterales
            val panelW = 8.dp.toPx()
            val panelH = 12.dp.toPx()
            val armLen = 3.dp.toPx()

            val colorPanel = if (estado == EstadoAvatar.Error) Color(0xFFE57373) else Color(0xFF0288D1)

            // Panel izquierdo
            val leftPanelLeft = centerSatX - bodySize / 2f - armLen - panelW
            val panelTop = centerSatY - panelH / 2f
            drawLine(
                color = Color(0xFF78909C),
                start = Offset(centerSatX - bodySize / 2f, centerSatY),
                end = Offset(leftPanelLeft + panelW, centerSatY),
                strokeWidth = 2.dp.toPx()
            )
            drawRect(
                color = colorPanel,
                topLeft = Offset(leftPanelLeft, panelTop),
                size = Size(panelW, panelH)
            )
            drawLine(
                color = Color(0xFFE0F7FA),
                start = Offset(leftPanelLeft, centerSatY),
                end = Offset(leftPanelLeft + panelW, centerSatY),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFFE0F7FA),
                start = Offset(leftPanelLeft + panelW / 2f, panelTop),
                end = Offset(leftPanelLeft + panelW / 2f, panelTop + panelH),
                strokeWidth = 1.dp.toPx()
            )

            // Panel derecho
            val rightPanelLeft = centerSatX + bodySize / 2f + armLen
            drawLine(
                color = Color(0xFF78909C),
                start = Offset(centerSatX + bodySize / 2f, centerSatY),
                end = Offset(rightPanelLeft, centerSatY),
                strokeWidth = 2.dp.toPx()
            )
            drawRect(
                color = colorPanel,
                topLeft = Offset(rightPanelLeft, panelTop),
                size = Size(panelW, panelH)
            )
            drawLine(
                color = Color(0xFFE0F7FA),
                start = Offset(rightPanelLeft, centerSatY),
                end = Offset(rightPanelLeft + panelW, centerSatY),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFFE0F7FA),
                start = Offset(rightPanelLeft + panelW / 2f, panelTop),
                end = Offset(rightPanelLeft + panelW / 2f, panelTop + panelH),
                strokeWidth = 1.dp.toPx()
            )

            // 4. Cuerpo central del satélite (cubo)
            val bodyColor = if (estado == EstadoAvatar.Error) Color(0xFFEF5350) else Color(0xFF90A4AE)
            drawRect(
                color = bodyColor,
                topLeft = Offset(centerSatX - bodySize / 2f, centerSatY - bodySize / 2f),
                size = Size(bodySize, bodySize)
            )
            drawRect(
                color = Color(0xFF37474F),
                topLeft = Offset(centerSatX - bodySize / 2f, centerSatY - bodySize / 2f),
                size = Size(bodySize, bodySize),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Pequeña luz LED en el centro
            val ledColor = if (estado == EstadoAvatar.Exito) {
                Color(0xFF7CF0A5)
            } else if (estado == EstadoAvatar.Error) {
                Color(0xFFFF8A8A)
            } else {
                Color(0xFF4FD8FF)
            }
            drawCircle(
                color = ledColor,
                radius = 2.dp.toPx(),
                center = Offset(centerSatX, centerSatY)
            )
        }
    }
}
