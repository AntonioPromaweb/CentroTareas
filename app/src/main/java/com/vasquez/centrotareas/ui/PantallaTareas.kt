package com.vasquez.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vasquez.centrotareas.model.CargaUi
import com.vasquez.centrotareas.model.PrimosUi
import com.vasquez.centrotareas.ui.theme.ErrorFondo
import com.vasquez.centrotareas.ui.theme.ErrorRojo
import com.vasquez.centrotareas.ui.theme.ExitoFondo
import com.vasquez.centrotareas.ui.theme.ExitoVerde
import com.vasquez.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val primos by vm.primos.collectAsState()
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    var textoN by rememberSaveable { mutableStateOf("5000000") }
    // Contador de la "prueba de vida": si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }

    FondoEspacial {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("CentroTareas", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    // imePadding: cuando sale el teclado, el área visible se reduce y se puede
                    // seguir desplazando hasta ver los botones y el resultado
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cálculo de estado para el avatar del cohete (if / else)
                val estadoCohete = if (corriendo) {
                    EstadoAvatar.Trabajando
                } else if (mensaje == "Terminó") {
                    EstadoAvatar.Exito
                } else {
                    EstadoAvatar.Reposo
                }

                TarjetaTarea(
                    titulo = "Tarea con progreso",
                    avatar = { AvatarCohete(estado = estadoCohete) }
                ) {
                    Button(onClick = { toques++ }) { Text("Tócame: $toques") }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Iniciar se deshabilita mientras corre; Cancelar solo se habilita mientras corre
                        Button(onClick = { vm.tareaLarga() }, enabled = !corriendo) { Text("Iniciar") }
                        OutlinedButton(onClick = { vm.cancelar() }, enabled = corriendo) { Text("Cancelar") }
                    }
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(progreso * 100).toInt()} %",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
                }

                // Cálculo de estado para el avatar del planeta (if / else)
                val estaCalculando = primos is PrimosUi.Calculando || primos is PrimosUi.Avance

                val estadoPlaneta = if (estaCalculando) {
                    EstadoAvatar.Trabajando
                } else if (primos is PrimosUi.Listo) {
                    EstadoAvatar.Exito
                } else {
                    EstadoAvatar.Reposo
                }

                TarjetaTarea(
                    titulo = "Contar números primos",
                    avatar = { AvatarPlaneta(estado = estadoPlaneta) }
                ) {
                    // Paso 5: el texto del campo vive en la pantalla (rememberSaveable sobrevive a rotar)
                    OutlinedTextField(
                        value = textoN,
                        onValueChange = { textoN = it.filter { c -> c.isDigit() }.take(9) },
                        label = { Text("Contar primos hasta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !estaCalculando,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    val n = textoN.toIntOrNull()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            // Se ignora un número vacío, menor que 2 o que ya está calculando
                            onClick = { if (n != null && n >= 2) vm.contar(n) },
                            enabled = !estaCalculando && n != null && n >= 2
                        ) { Text("Contar") }
                        OutlinedButton(
                            onClick = { vm.cancelarPrimos() },
                            enabled = estaCalculando
                        ) { Text("Cancelar") }
                    }
                    // Un when exhaustivo sobre el estado: la pantalla solo dibuja lo que dice el estado
                    when (val estado = primos) {
                        PrimosUi.Inicial -> Text("Escribe un número y pulsa Contar")
                        PrimosUi.Calculando -> {
                            CircularProgressIndicator()
                            Text("Calculando...")
                        }
                        is PrimosUi.Avance -> {
                            LinearProgressIndicator(
                                progress = { estado.porcentaje / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Calculando... ${estado.porcentaje} %")
                            // Contador en vivo: sube mientras el Repository sigue emitiendo avances
                            Text(
                                "Primos encontrados: ${estado.encontrados}",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        is PrimosUi.Listo -> CajaResultado(
                            "Hay ${estado.cantidad} primos hasta $n\nTiempo: ${estado.milisegundos} ms",
                            ExitoFondo, ExitoVerde
                        )
                        PrimosUi.Cancelado -> Text("Cancelado")
                    }
                }

                // Cálculo de estado para el avatar del satélite (if / else)
                val estadoSatelite = if (carga is CargaUi.Cargando) {
                    EstadoAvatar.Trabajando
                } else if (carga is CargaUi.Exito) {
                    EstadoAvatar.Exito
                } else if (carga is CargaUi.Error) {
                    EstadoAvatar.Error
                } else {
                    EstadoAvatar.Reposo
                }

                TarjetaTarea(
                    titulo = "Dos cargas",
                    avatar = { AvatarSatelite(estado = estadoSatelite) }
                ) {
                    // Paso 6: los botones se deshabilitan mientras carga para no lanzar dos cargas
                    val cargando = carga is CargaUi.Cargando
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { vm.cargarUnaPorUna() }, enabled = !cargando) { Text("Una por una") }
                        Button(onClick = { vm.cargarALaVez() }, enabled = !cargando) { Text("A la vez") }
                    }
                    // Paso 7: interruptor para simular que los pedidos fallan
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Simular error")
                        Switch(
                            checked = simularError,
                            onCheckedChange = { vm.cambiarSimularError(it) },
                            enabled = !cargando
                        )
                    }
                    when (val estado = carga) {
                        CargaUi.Inicial -> Text("Elige cómo cargar")
                        CargaUi.Cargando -> {
                            CircularProgressIndicator()
                            Text("Cargando...")
                        }
                        is CargaUi.Exito -> CajaResultado(
                            "Modo: ${estado.modo}\nPerfil: ${estado.perfil}\n" +
                                "Pedidos: ${estado.pedidos}\nTiempo: ${estado.milisegundos} ms",
                            ExitoFondo, ExitoVerde
                        )
                        is CargaUi.Error -> {
                            CajaResultado(estado.mensaje, ErrorFondo, ErrorRojo)
                            // Reintentar vuelve a cargar "A la vez"
                            Button(onClick = { vm.reintentar() }) { Text("Reintentar") }
                        }
                    }
                }
            }
        }
    }
}

// Caja de color para mostrar un resultado (verde = éxito)
@Composable
private fun CajaResultado(texto: String, fondo: Color, colorTexto: Color) {
    Text(
        text = texto,
        color = colorTexto,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(8.dp))
            .padding(12.dp)
    )
}

@Composable
private fun TarjetaTarea(
    titulo: String,
    avatar: @Composable () -> Unit = {},
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xCC111A3D),
            // Sin esto el texto saldría negro sobre el fondo oscuro
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, Color(0x334FD8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatar()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            contenido()
        }
    }
}
