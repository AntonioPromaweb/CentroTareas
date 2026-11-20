package com.vasquez.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.vasquez.centrotareas.model.PrimosUi
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
    var textoN by rememberSaveable { mutableStateOf("5000000") }
    // Contador de la "prueba de vida": si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaTarea(titulo = "Tarea con progreso") {
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

            TarjetaTarea(titulo = "Contar números primos") {
                // Paso 5: el texto del campo vive en la pantalla (rememberSaveable sobrevive a rotar)
                OutlinedTextField(
                    value = textoN,
                    onValueChange = { textoN = it.filter { c -> c.isDigit() }.take(9) },
                    label = { Text("Contar primos hasta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = primos !is PrimosUi.Calculando
                )
                val n = textoN.toIntOrNull()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        // Se ignora un número vacío, menor que 2 o que ya está calculando
                        onClick = { if (n != null && n >= 2) vm.contar(n) },
                        enabled = primos !is PrimosUi.Calculando && n != null && n >= 2
                    ) { Text("Contar") }
                    OutlinedButton(
                        onClick = { vm.cancelarPrimos() },
                        enabled = primos is PrimosUi.Calculando
                    ) { Text("Cancelar") }
                }
                // Un when exhaustivo sobre el estado: la pantalla solo dibuja lo que dice el estado
                when (val estado = primos) {
                    PrimosUi.Inicial -> Text("Escribe un número y pulsa Contar")
                    PrimosUi.Calculando -> {
                        CircularProgressIndicator()
                        Text("Calculando...")
                    }
                    is PrimosUi.Listo -> CajaResultado(
                        "Hay ${estado.cantidad} primos hasta $n\nTiempo: ${estado.milisegundos} ms",
                        ExitoFondo, ExitoVerde
                    )
                    PrimosUi.Cancelado -> Text("Cancelado")
                }
            }

            TarjetaTarea(titulo = "Dos cargas") {
                // TODO Pasos 6 y 7: botones Una por una / A la vez, resultado y error
                Text(text = "Pendiente: Pasos 6 y 7", style = MaterialTheme.typography.bodyMedium)
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
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            contenido()
        }
    }
}
