package com.vasquez.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasquez.centrotareas.data.TareasRepository
import com.vasquez.centrotareas.model.CargaUi
import com.vasquez.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: progreso de 0f a 1f. Se expone solo la versión de lectura (StateFlow);
    // el único que lo modifica es el ViewModel.
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 4: guardamos el Job que devuelve launch para poder cancelarlo, y un estado
    // "corriendo" que la pantalla usa para habilitar o deshabilitar los botones.
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    // Pasos 2 a 4: viewModelScope.launch arranca una corrutina (empieza en el hilo principal)
    // y withContext cambia a un hilo de segundo plano para el trabajo. delay SUSPENDE la
    // corrutina sin bloquear ningún hilo, así que la pantalla sigue dibujando y atendiendo
    // toques. 100 vueltas x 40 ms = 4 segundos, y cada vuelta avanza la barra un 1 %.
    fun tareaLarga() {
        // Doble inicio: si ya hay una tarea corriendo, no lanzamos otra
        if (_corriendo.value) return
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                withContext(Dispatchers.Default) {
                    Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                    for (i in 1..100) {
                        delay(40) // delay revisa la cancelación en cada vuelta
                        _progreso.value = i / 100f
                    }
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                // Se ejecuta siempre: si terminó bien o si la cancelaron
                _corriendo.value = false
            }
        }
    }

    // Cancela el Job: la barra se queda donde estaba y el mensaje pasa a Cancelado
    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    // ----- Contar primos (paso 5)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    // Paso 5: Job propio para poder cancelar el conteo sin afectar a la otra tarea
    private var trabajoPrimos: Job? = null

    // Cuenta primos hasta "hasta", mide el tiempo y publica Listo o Cancelado
    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando) return // evita doble inicio
        _primos.value = PrimosUi.Calculando
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                val cantidad = repo.contarPrimos(hasta)
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // siempre se vuelve a lanzar para no romper la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // TODO Paso 6: fun cargarUnaPorUna() y fun cargarALaVez()
    // TODO Paso 7: simularError y manejo de errores
}
